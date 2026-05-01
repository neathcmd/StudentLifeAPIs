package com.studentlife.StudentLifeAPIs.Service.Impl;

import com.studentlife.StudentLifeAPIs.DTO.Request.ChatMessageRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.NotificationRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.*;
import com.studentlife.StudentLifeAPIs.Entity.*;
import com.studentlife.StudentLifeAPIs.Enum.AssignmentMemberStatus;
import com.studentlife.StudentLifeAPIs.Enum.NotificationType;
import com.studentlife.StudentLifeAPIs.Mapper.GroupMessageMapper;
import com.studentlife.StudentLifeAPIs.Repository.AssignmentMemberRepository;
import com.studentlife.StudentLifeAPIs.Repository.AssignmentRepository;
import com.studentlife.StudentLifeAPIs.Repository.GroupChatMemberRepository;
import com.studentlife.StudentLifeAPIs.Repository.GroupMessageRepository;
import com.studentlife.StudentLifeAPIs.Service.GroupChatService;
import com.studentlife.StudentLifeAPIs.Service.NotificationService;
import com.studentlife.StudentLifeAPIs.Service.OneSignalService;
import com.studentlife.StudentLifeAPIs.Service.PresenceService;
import com.studentlife.StudentLifeAPIs.Utils.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.studentlife.StudentLifeAPIs.Exception.ErrorsExceptionFactory.forbidden;
import static com.studentlife.StudentLifeAPIs.Exception.ErrorsExceptionFactory.notFound;

// Handles all group chat logic:
// - Get list of groups the current user belongs to
// - Send a message inside a group
// - Fetch chat history for a group
// - Clear chat history (owner only)
//
// When a message is sent, 3 layers of notification fire:
// 1. WebSocket   → instant delivery if the recipient has the app open
// 2. In-app (DB) → saves a Notification record so the bell icon shows unread count
// 3. OneSignal   → push notification for offline users / closed app
@Slf4j
@Service
@RequiredArgsConstructor
public class GroupChatServiceImpl implements GroupChatService {

    private final GroupMessageRepository groupMessageRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentMemberRepository assignmentMemberRepository;
    private final AuthUtil authUtil;
    private final SimpMessagingTemplate messagingTemplate;
    private final GroupMessageMapper groupMessageMapper;
    private final OneSignalService oneSignalService;
    private final NotificationService notificationService;
    private final GroupChatMemberRepository groupChatMemberRepository;
    private final PresenceService presenceService;

    // Returns all group chats the current user has access to
    // A user can see a group if they are the owner OR an accepted member
    // For each group it also returns member count and last message preview (truncated to 50 chars)
    // Uses a Set<Long> to avoid duplicate groups in case a user appears in both lists
    @Override
    public ApiResponse<List<GroupResponse>> getMyGroups() {
        Users currentUser = authUtil.getAuthenticatedUser();
        Long userId = currentUser.getId();


        List<Assignments> ownedAssignments = assignmentRepository.findAllAccessibleByUserId(userId);


        List<Assignments> memberAssignments = assignmentMemberRepository
                .findByUserIdAndStatus(userId, AssignmentMemberStatus.ACCEPTED)
                .stream()
                .map(AssignmentMember::getAssignment)
                .toList();

        // Merge both lists, removing duplicates using a Set to track seen IDs
        Set<Long> seen = new HashSet<>();
        List<Assignments> allGroups = new java.util.ArrayList<>();

        for (Assignments a : ownedAssignments) {
            if (seen.add(a.getId())) {
                allGroups.add(a);
            }
        }

        for (Assignments a : memberAssignments) {
            if (seen.add(a.getId())) {
                allGroups.add(a);
            }
        }

        // Build the response DTO for each group
        List<GroupResponse> groups = allGroups.stream().map(a -> {

            // +1 for the owner who is not stored in the members table
            int memberCount = (int) assignmentMemberRepository
                    .findByAssignmentIdAndStatus(a.getId(), AssignmentMemberStatus.ACCEPTED)
                    .size() + 1;

            // Get all messages sorted oldest first to find the last one
            List<GroupMessage> messages = groupMessageRepository
                    .findByAssignmentIdOrderByCreatedAtAsc(a.getId());

            String lastMessage = null;
            String lastMessageTime = null;
            String lastMessageSender = null;

            if (!messages.isEmpty()) {
                GroupMessage last = messages.get(messages.size() - 1);

                lastMessage = last.getContent().length() > 50
                        ? last.getContent().substring(0, 50) + "…"
                        : last.getContent();
                lastMessageTime = last.getCreatedAt().toString();
                lastMessageSender = last.getSender().getFullname();
            }

            return GroupResponse.builder()
                    .assignmentId(a.getId())
                    .assignmentTitle(a.getTitle())
                    .subject(a.getSubject())
                    .ownerName(a.getUser().getFullname())
                    .ownerUsername(a.getUser().getUsername())
                    .memberCount(memberCount)
                    .lastMessage(lastMessage)
                    .lastMessageTime(lastMessageTime)
                    .lastMessageSender(lastMessageSender)
                    .build();
        }).toList();

        return new ApiResponse<>(200, true, "Group retrieved successfully", groups);
    }

    @Override
    @Transactional
    public GroupMessageResponse sendMessage(ChatMessageRequest request, Long senderId) {

        //  Find the assignment or throw 404
        Assignments assignment = assignmentRepository.findById(request.getAssignmentId())
                .orElseThrow(() -> notFound("Assignment not found."));

        // Check if the sender is the owner or an accepted member
        boolean isOwner = assignment.getUser().getId().equals(senderId);
        boolean isMember = assignmentMemberRepository
                .findByAssignmentIdAndUserId(request.getAssignmentId(), senderId)
                .map(m -> m.getStatus() == AssignmentMemberStatus.ACCEPTED)
                .orElse(false);

        if (!isOwner && !isMember) {
            throw forbidden("You are not a member of this group.");
        }

        // Resolve sender entity safely
        // Owner → grab directly from assignment
        // Member → look up from AssignmentMember table
        Users sender;
        if (isOwner) {
            sender = assignment.getUser();
        } else {
            sender = assignmentMemberRepository
                    .findByAssignmentIdAndUserId(request.getAssignmentId(), senderId)
                    .orElseThrow(() -> notFound("Member not found."))
                    .getUser();
        }

        // Step 4: Build and save the message to DB
        GroupMessage message = GroupMessage.builder()
                .assignmentId(request.getAssignmentId())
                .sender(sender)
                .content(request.getContent())
                .build();

        GroupMessage saved = groupMessageRepository.save(message);
        GroupMessageResponse response = groupMessageMapper.toResponse(saved);

        // Broadcast message to all users subscribed to this group's WebSocket topic
        messagingTemplate.convertAndSend(
                "/topic/group/" + request.getAssignmentId(),
                response
        );

        List<AssignmentMember> members = assignmentMemberRepository
                .findByAssignmentIdAndStatus(request.getAssignmentId(), AssignmentMemberStatus.ACCEPTED);

        // Notify the owner if they are NOT the sender
        if (!assignment.getUser().getId().equals(senderId)) {
            NotificationRequest notificationRequest = new NotificationRequest();
            notificationRequest.setTitle(sender.getFullname());
            notificationRequest.setMessage(request.getContent());
            notificationService.sendNotification(notificationRequest, NotificationType.CHAT, assignment.getUser());
        }

        // Notify each accepted member — skip the sender so they don't notify themselves
        for (AssignmentMember member : members) {
            if (!member.getUser().getId().equals(senderId)) {
                NotificationRequest notificationRequest = new NotificationRequest();
                notificationRequest.setTitle(sender.getFullname());
                notificationRequest.setMessage(request.getContent());
                notificationService.sendNotification(notificationRequest, NotificationType.CHAT, member.getUser());
            }
        }


        // sendPushToUser() silently skips if oneSignalPlayerId is null (user hasn't registered yet)
        // Notify the owner if they are NOT the sender
        Users owner = assignment.getUser();
        if (!owner.getId().equals(senderId)) {
            oneSignalService.sendPushToUser(
                    owner.getOneSignalPlayerId(),
                    sender.getFullname(),
                    request.getContent()
            );
        }

        // Notify each accepted member — skip the sender
        for (AssignmentMember member : members) {
            if (!member.getUser().getId().equals(senderId)) {
                oneSignalService.sendPushToUser(
                        member.getUser().getOneSignalPlayerId(),
                        sender.getFullname(),
                        request.getContent()
                );
            }
        }

        log.info("[Chat] Message sent in group {} by user {}", request.getAssignmentId(), senderId);

        return response;
    }

    // Returns all messages for a group in chronological order (oldest first)
    // Only accessible to the group owner or accepted members — throws 403 otherwise
    // Used by the frontend when a user opens a group chat to load previous messages
    @Override
    public ApiResponse<List<GroupMessageResponse>> getChatHistory(Long assignmentId) {
        Users currentUser = authUtil.getAuthenticatedUser();

        Assignments assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> notFound("Assignment not found."));

        // Check access — must be owner or accepted member
        boolean isOwner = assignment.getUser().getId().equals(currentUser.getId());
        boolean isMember = assignmentMemberRepository
                .findByAssignmentIdAndUserId(assignmentId, currentUser.getId())
                .map(m -> m.getStatus() == AssignmentMemberStatus.ACCEPTED)
                .orElse(false);

        if (!isOwner && !isMember) {
            throw forbidden("You are not a member of this group.");
        }

        List<GroupMessageResponse> messages = groupMessageMapper.toResponseList(
                groupMessageRepository.findByAssignmentIdOrderByCreatedAtAsc(assignmentId)
        );

        return new ApiResponse<>(200, true, "Chat history retrieved.", messages);
    }

    // Deletes ALL messages in a group chat permanently
    // Only the assignment OWNER can do this — throws 403 if a regular member tries
    // Used as a moderation tool for the group owner to wipe the entire chat
    @Override
    @Transactional
    public ApiResponse<?> clearChatHistory(Long assignmentId) {
        Users currentUser = authUtil.getAuthenticatedUser();

        Assignments assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> notFound("Assignment not found."));

        if (!assignment.getUser().getId().equals(currentUser.getId())) {
            throw forbidden("Only the assignment owner can clear chat history.");
        }

        groupMessageRepository.deleteByAssignmentId(assignmentId);
        log.info("[Chat] History cleared for group {} by user {}", assignmentId, currentUser.getId());

        return new ApiResponse<>(200, true, "Chat history cleared");
    }

    @Override
    public ApiResponse<List<MemberResponse>> getGroupMember(Long assignmentId) {

        Assignments assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> notFound("Assignment not found."));

        Set<Long> onlineIds = presenceService.getOnlineUsers(assignmentId);

        // Accepted members from assignment_members table
        List<AssignmentMember> members = assignmentMemberRepository
                .findByAssignmentIdAndStatus(assignmentId, AssignmentMemberStatus.ACCEPTED);

        List<MemberResponse> responses = new java.util.ArrayList<>();

        // Always add the owner first
        Users owner = assignment.getUser();
        responses.add(MemberResponse.builder()
                .id(owner.getId())
                .fullname(owner.getFullname())
                .username(owner.getUsername())
                .email(owner.getEmail())
                .university(owner.getUniversity())
                .major(owner.getMajor())
                .academicYear(owner.getAcademicYear())
                .online(onlineIds.contains(owner.getId()))
                .build());

        // Then add each accepted member
        for (AssignmentMember m : members) {
            Users u = m.getUser();
            responses.add(MemberResponse.builder()
                    .id(u.getId())
                    .fullname(u.getFullname())
                    .username(u.getUsername())
                    .email(u.getEmail())
                    .university(u.getUniversity())
                    .major(u.getMajor())
                    .academicYear(u.getAcademicYear())
                    .online(onlineIds.contains(u.getId()))
                    .build());
        }

        return new ApiResponse<>(
                200,
                true,
                "Get group member successfully",
                responses
        );
    }


    @Override
    public void userJoined(Long assignmentId, Long userId, String username) {
        presenceService.userJoined(assignmentId, userId);
        broadcastPresence(assignmentId);
    }

    public void userLeft(Long assignmentId, Long userId, String username) {
        presenceService.userLeft(assignmentId, userId);
        broadcastPresence(assignmentId);
    }

    private void broadcastPresence(Long assignmentId) {
        Set<Long> onlineIds = presenceService.getOnlineUsers(assignmentId);
        PresenceEventResponse eventResponse = PresenceEventResponse.builder()
                .assignmentId(assignmentId)
                .onlineCount(onlineIds.size())
                .onlineUserIds(onlineIds)
                .build();
        messagingTemplate.convertAndSend(
                "/topic/group/" + assignmentId + "/presence", eventResponse
        );
    }

}