package com.studentlife.StudentLifeAPIs.Service.Impl;

import com.studentlife.StudentLifeAPIs.DTO.Request.ChatMessageRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.GroupMessageResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.GroupResponse;
import com.studentlife.StudentLifeAPIs.Entity.AssignmentMember;
import com.studentlife.StudentLifeAPIs.Entity.Assignments;
import com.studentlife.StudentLifeAPIs.Entity.GroupMessage;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Enum.AssignmentMemberStatus;
import com.studentlife.StudentLifeAPIs.Mapper.GroupMessageMapper;
import com.studentlife.StudentLifeAPIs.Repository.AssignmentMemberRepository;
import com.studentlife.StudentLifeAPIs.Repository.AssignmentRepository;
import com.studentlife.StudentLifeAPIs.Repository.GroupMessageRepository;
import com.studentlife.StudentLifeAPIs.Service.GroupChatService;
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

    @Override
    public ApiResponse<List<GroupResponse>> getMyGroups() {
        Users currentUser = authUtil.getAuthenticatedUser();
        Long userId = currentUser.getId();

        // Assignments I own
        List<Assignments> ownedAssignments = assignmentRepository.findByUserId(userId);

        // Assignments I was invited to and accepted
        // FIX 1: was passing userId as assignmentId — now uses correct method
        List<Assignments> memberAssignments = assignmentMemberRepository
                .findByUserIdAndStatus(userId, AssignmentMemberStatus.ACCEPTED)
                .stream()
                .map(AssignmentMember::getAssignment)
                .toList();

        // Merge without duplicates
        Set<Long> seen = new HashSet<>();
        List<Assignments> allGroups = new java.util.ArrayList<>();

        // FIX 2: owner always sees their group, no hasMembers gate
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

        List<GroupResponse> groups = allGroups.stream().map(a -> {
            int memberCount = (int) assignmentMemberRepository
                    .findByAssignmentIdAndStatus(a.getId(), AssignmentMemberStatus.ACCEPTED)
                    .size() + 1; // +1 for owner

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

        return new ApiResponse<>(
                200,
                true,
                "Group retrieved successfully",
                groups
        );
    }

    @Override
    @Transactional
    public GroupMessageResponse sendMessage(ChatMessageRequest request, Long senderId) {

        Assignments assignment = assignmentRepository.findById(request.getAssignmentId())
                .orElseThrow(() -> notFound("Assignment not found."));

        boolean isOwner = assignment.getUser().getId().equals(senderId);
        boolean isMember = assignmentMemberRepository
                .findByAssignmentIdAndUserId(request.getAssignmentId(), senderId)
                .map(m -> m.getStatus() == AssignmentMemberStatus.ACCEPTED)
                .orElse(false);

        if (!isOwner && !isMember) {
            throw forbidden("You are not a member of this group.");
        }

        // FIX 4: safe sender resolution, no raw .get()
        Users sender;
        if (isOwner) {
            sender = assignment.getUser();
        } else {
            sender = assignmentMemberRepository
                    .findByAssignmentIdAndUserId(request.getAssignmentId(), senderId)
                    .orElseThrow(() -> notFound("Member not found."))
                    .getUser();
        }

        GroupMessage message = GroupMessage.builder()
                .assignmentId(request.getAssignmentId())
                .sender(sender)
                .content(request.getContent())
                .build();

        GroupMessage saved = groupMessageRepository.save(message);
        GroupMessageResponse response = groupMessageMapper.toResponse(saved);

        messagingTemplate.convertAndSend(
                "/topic/group/" + request.getAssignmentId(),
                response
        );

        log.info("[Chat] Message sent in group {} by user {}", request.getAssignmentId(), senderId);

        return response;
    }

    @Override
    public ApiResponse<List<GroupMessageResponse>> getChatHistory(Long assignmentId) {
        Users currentUser = authUtil.getAuthenticatedUser();

        Assignments assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> notFound("Assignment not found."));

        // FIX 3: was comparing isActive flags, now correctly compares IDs
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

        return new ApiResponse<>(
                200,
                true,
                "Chat history retrieved.",
                messages
        );
    }

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

        return new ApiResponse<>(
                200,
                true,
                "Chat history cleared"
        );
    }
}