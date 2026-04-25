package com.studentlife.StudentLifeAPIs.Service.Impl;

import com.studentlife.StudentLifeAPIs.DTO.Request.ChatMessageRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.NotificationRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.GroupMessageResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.GroupResponse;
import com.studentlife.StudentLifeAPIs.Entity.AssignmentMember;
import com.studentlife.StudentLifeAPIs.Entity.Assignments;
import com.studentlife.StudentLifeAPIs.Entity.GroupMessage;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Enum.AssignmentMemberStatus;
import com.studentlife.StudentLifeAPIs.Enum.NotificationType;
import com.studentlife.StudentLifeAPIs.Mapper.GroupMessageMapper;
import com.studentlife.StudentLifeAPIs.Repository.AssignmentMemberRepository;
import com.studentlife.StudentLifeAPIs.Repository.AssignmentRepository;
import com.studentlife.StudentLifeAPIs.Repository.GroupMessageRepository;
import com.studentlife.StudentLifeAPIs.Service.GroupChatService;
import com.studentlife.StudentLifeAPIs.Service.NotificationService;
import com.studentlife.StudentLifeAPIs.Service.OneSignalService;
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

    private final GroupMessageRepository groupMessageRepository;         // DB access for chat messages
    private final AssignmentRepository assignmentRepository;             // DB access for assignments (groups)
    private final AssignmentMemberRepository assignmentMemberRepository; // DB access for group members
    private final AuthUtil authUtil;                                      // gets the currently logged-in user
    private final SimpMessagingTemplate messagingTemplate;               // sends WebSocket messages
    private final GroupMessageMapper groupMessageMapper;                 // converts GroupMessage entity → DTO
    private final OneSignalService oneSignalService;                     // sends push notifications via OneSignal REST API
    private final NotificationService notificationService;               // saves in-app notifications to DB + fires WebSocket bell

    // Returns all group chats the current user has access to
    // A user can see a group if they are the owner OR an accepted member
    // For each group it also returns member count and last message preview (truncated to 50 chars)
    // Uses a Set<Long> to avoid duplicate groups in case a user appears in both lists
    @Override
    public ApiResponse<List<GroupResponse>> getMyGroups() {
        Users currentUser = authUtil.getAuthenticatedUser();
        Long userId = currentUser.getId();

        // Fetch assignments this user owns
        List<Assignments> ownedAssignments = assignmentRepository.findAllAccessibleByUserId(userId);

        // Fetch assignments this user was invited to and accepted
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

                // Truncate preview to 50 chars so the UI sidebar doesn't overflow
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

    // Sends a chat message inside a group and notifies all other members
    // Flow:
    // 1. Find the assignment or throw 404
    // 2. Check sender is owner or accepted member — throw 403 if not
    // 3. Resolve sender entity safely (owner path vs member path)
    // 4. Save the message to DB
    // 5. Broadcast via WebSocket → online users get it instantly in the chat UI
    // 6. Notify everyone except the sender via in-app notification (DB + bell)
    // 7. Notify everyone except the sender via OneSignal push (for offline users)
    @Override
    @Transactional
    public GroupMessageResponse sendMessage(ChatMessageRequest request, Long senderId) {

        // Step 1: Find the assignment or throw 404
        Assignments assignment = assignmentRepository.findById(request.getAssignmentId())
                .orElseThrow(() -> notFound("Assignment not found."));

        // Step 2: Check if the sender is the owner or an accepted member
        boolean isOwner = assignment.getUser().getId().equals(senderId);
        boolean isMember = assignmentMemberRepository
                .findByAssignmentIdAndUserId(request.getAssignmentId(), senderId)
                .map(m -> m.getStatus() == AssignmentMemberStatus.ACCEPTED)
                .orElse(false);

        if (!isOwner && !isMember) {
            throw forbidden("You are not a member of this group.");
        }

        // Step 3: Resolve sender entity safely
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

        // Step 5: Broadcast message to all users subscribed to this group's WebSocket topic
        // Frontend listens on: /topic/group/{assignmentId}
        messagingTemplate.convertAndSend(
                "/topic/group/" + request.getAssignmentId(),
                response
        );

        // Fetch all accepted members — used in both notification loops below
        List<AssignmentMember> members = assignmentMemberRepository
                .findByAssignmentIdAndStatus(request.getAssignmentId(), AssignmentMemberStatus.ACCEPTED);

        // Step 6: In-app notification (saves to DB + fires WebSocket bell update)
        // Notify the owner if they are NOT the sender
        if (!assignment.getUser().getId().equals(senderId)) {
            NotificationRequest notificationRequest = new NotificationRequest();
            notificationRequest.setTitle(sender.getFullname());   // sender name shown as notification title
            notificationRequest.setMessage(request.getContent()); // message content shown as notification body
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

        // Step 7: OneSignal push notification (for users who are offline or have the app closed)
        // sendPushToUser() silently skips if oneSignalPlayerId is null (user hasn't registered yet)
        // Notify the owner if they are NOT the sender
        Users owner = assignment.getUser();
        if (!owner.getId().equals(senderId)) {
            oneSignalService.sendPushToUser(
                    owner.getOneSignalPlayerId(),
                    sender.getFullname(),  // notification title = sender's name
                    request.getContent()  // notification body = message text
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

        // Find the assignment or throw 404
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

        // Fetch messages and map to response DTOs
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

        // Find the assignment or throw 404
        Assignments assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> notFound("Assignment not found."));

        // Only the owner can clear history — block everyone else
        if (!assignment.getUser().getId().equals(currentUser.getId())) {
            throw forbidden("Only the assignment owner can clear chat history.");
        }

        // Delete all messages for this group from DB
        groupMessageRepository.deleteByAssignmentId(assignmentId);
        log.info("[Chat] History cleared for group {} by user {}", assignmentId, currentUser.getId());

        return new ApiResponse<>(200, true, "Chat history cleared");
    }
}