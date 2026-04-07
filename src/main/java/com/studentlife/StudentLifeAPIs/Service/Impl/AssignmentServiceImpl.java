package com.studentlife.StudentLifeAPIs.Service.Impl;

import com.studentlife.StudentLifeAPIs.DTO.Request.CreateAssignmentRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.InviteRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.NotificationRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.UpdateProgressRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.AssignmentMemberResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.AssignmentResponse;
import com.studentlife.StudentLifeAPIs.Entity.AssignmentMember;
import com.studentlife.StudentLifeAPIs.Entity.Assignments;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Enum.AssignmentMemberStatus;
import com.studentlife.StudentLifeAPIs.Enum.AssignmentStatus;
import com.studentlife.StudentLifeAPIs.Enum.NotificationType;
import com.studentlife.StudentLifeAPIs.Mapper.AssignmentMapper;
import com.studentlife.StudentLifeAPIs.Repository.AssignmentMemberRepository;
import com.studentlife.StudentLifeAPIs.Repository.AssignmentRepository;
import com.studentlife.StudentLifeAPIs.Repository.ScheduleRepository;
import com.studentlife.StudentLifeAPIs.Repository.UserRepository;
import com.studentlife.StudentLifeAPIs.Service.AssignmentService;
import com.studentlife.StudentLifeAPIs.Service.EmailService;
import com.studentlife.StudentLifeAPIs.Service.NotificationService;
import com.studentlife.StudentLifeAPIs.Service.ScheduleService;
import com.studentlife.StudentLifeAPIs.Utils.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.studentlife.StudentLifeAPIs.Exception.ErrorsExceptionFactory.*;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AuthUtil authUtil;
    private final AssignmentMapper assignmentMapper;
    private final ScheduleService scheduleService;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final AssignmentMemberRepository assignmentMemberRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    @Override
    public ApiResponse<AssignmentResponse> createAssignment(CreateAssignmentRequest request) {
        Users currentUser = authUtil.getAuthenticatedUser();

        Assignments assignment = assignmentMapper.toEntity(request);
        assignment.setUser(currentUser);

        assignmentRepository.save(assignment);

        Long scheduleId = scheduleService.createAssignmentSchedule(
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getDueDate(),
                assignment.getId(),
                currentUser
        );

        AssignmentResponse response = assignmentMapper.toResponse(assignment);
        response.setScheduleId(scheduleId);

        return new ApiResponse<>(
                201,
                true,
                "Assignment created successfully.",
                response
        );
    }

    @Override
    public ApiResponse<List<AssignmentResponse>> getMyAssignments() {

        Users currentUser = authUtil.getAuthenticatedUser();

        List<AssignmentResponse> responses = assignmentRepository
                .findByUserId(currentUser.getId())
                .stream()
                .map(assignmentMapper::toResponse)
                .toList();

        return new ApiResponse<>(
                200,
                true,
                "Get all assignment successfully.",
                responses
        );
    }

    @Override
    public ApiResponse<AssignmentResponse> getAssignmentById(Long id) {

        Users currentUser = authUtil.getAuthenticatedUser();

        Assignments assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> notFound("Assignment not found."));

        if (!assignment.getUser().getId().equals(currentUser.getId())) {
            throw forbidden("You do not have access to this resource.");
        }

        return new ApiResponse<>(
                200,
                true,
                "Get assignment successfully.",
                assignmentMapper.toResponse(assignment)
        );
    }

    @Override
    public ApiResponse<AssignmentResponse> updateProgress(Long id, UpdateProgressRequest request) {
        Users currentUser = authUtil.getAuthenticatedUser();

        Assignments assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> notFound("Assignment not found."));

        if (!assignment.getUser().getId().equals(currentUser.getId())) {
            throw forbidden("You do not have access to this resource.");
        }

        assignment.setProgress(request.getProgress());

        if (request.getProgress() == 100) {
            assignment.setStatus(AssignmentStatus.COMPLETED);
        } else if (request.getProgress() > 0) {
            assignment.setStatus(AssignmentStatus.IN_PROGRESS);
        } else {
            assignment.setStatus(AssignmentStatus.PENDING);
        }

        assignmentRepository.save(assignment);

        return new ApiResponse<>(
                200,
                true,
                "Progress updated successfully.",
                assignmentMapper.toResponse(assignment)
        );
    }

    @Override
    @Transactional
    public ApiResponse<?> deleteAssignment(Long id) {
        Users currentUser = authUtil.getAuthenticatedUser();

        Assignments assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> notFound("Assignment not found."));

        if (!assignment.getUser().getId().equals(currentUser.getId())) {
            throw forbidden("You do not have access to this resource.");
        }

        scheduleRepository.deleteByAssignmentId(id);
        assignmentRepository.delete(assignment);

        return new ApiResponse<>(
                200,
                true,
                "Assignment deleted successfully."
        );
    }

    @Override
    public ApiResponse<?> inviteUser(Long assignmentId, InviteRequest request) {
        Users currentUser = authUtil.getAuthenticatedUser();

        Assignments assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> notFound("Assignment not found."));

        if (!assignment.getUser().getId().equals(currentUser.getId())) {
            throw forbidden("Only the owner can invite members");
        }

        Users invitedUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> notFound("User with email " + request.getEmail() + "not found."));

        if (invitedUser.getId().equals(currentUser.getId())) {
            throw validation("You cannot invite yourself.");
        }

        if (assignmentMemberRepository.existsByAssignmentIdAndUserId(assignmentId, invitedUser.getId())) {
            throw validation("This user has already been invited.");
        }

        AssignmentMember member = AssignmentMember.builder()
                .assignment(assignment)
                .user(invitedUser)
                .status(AssignmentMemberStatus.INVITED)
                .build();

        assignmentMemberRepository.save(member);

        emailService.sendInviteEmail(
                invitedUser.getEmail(),
                currentUser.getFullname(),
                assignment.getTitle(),
                assignment.getId()
        );

        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setTitle("Assignment Invitation");
        notificationRequest.setMessage(currentUser.getFullname() + " invited you to join \"" + assignment.getTitle() + "\".");

        notificationService.sendNotification(notificationRequest, NotificationType.INVITE, invitedUser);

        return new ApiResponse<>(
                200,
                true,
                "Invitation sent successfully."
        );
    }

    @Override
    @Transactional
    public ApiResponse<?> acceptInvite(Long assignmentId) {
        Users currentUser = authUtil.getAuthenticatedUser();

        AssignmentMember member = assignmentMemberRepository
                .findByAssignmentIdAndUserId(assignmentId, currentUser.getId())
                .orElseThrow(() -> notFound("Invitation not found."));

        if (member.getStatus() != AssignmentMemberStatus.INVITED) {
            throw validation("Invitation already responded to.");
        }

        member.setStatus(AssignmentMemberStatus.ACCEPTED);
        assignmentMemberRepository.save(member);

        Assignments assignment = member.getAssignment();

        emailService.sendInviteAcceptedEmail(
                assignment.getUser().getEmail(),
                currentUser.getFullname(),
                assignment.getTitle()
        );

        scheduleService.createAssignmentSchedule(
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getDueDate(),
                assignment.getId(),
                currentUser
        );

        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setTitle("Invite Accepted");
        notificationRequest.setMessage(currentUser.getFullname() + " accepted your invitation to \"" + assignment.getTitle() + "\".");
        notificationService.sendNotification(notificationRequest, NotificationType.INVITE, assignment.getUser());

        return new ApiResponse<>(
                200,
                true,
                "Invite accepted successfully."
        );
    }

    @Override
    public ApiResponse<?> declineInvite(Long assignmentId) {
        Users currentUser = authUtil.getAuthenticatedUser();

        AssignmentMember member = assignmentMemberRepository
                .findByAssignmentIdAndUserId(assignmentId, currentUser.getId())
                .orElseThrow(() -> notFound("Invitation not found."));

        if (member.getStatus() != AssignmentMemberStatus.INVITED) {
            throw validation("Invitation already responded to.");
        }

        member.setStatus(AssignmentMemberStatus.DECLINED);
        assignmentMemberRepository.save(member);

        Assignments assignment = member.getAssignment();

        emailService.sendDeclinedEmail(
                assignment.getUser().getEmail(),
                currentUser.getFullname(),
                assignment.getTitle()
        );

        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setTitle("Invite Declined");
        notificationRequest.setMessage(currentUser.getFullname() + " declined your invitation to \"" + assignment.getTitle() + "\".");
        notificationService.sendNotification(notificationRequest, NotificationType.INVITE, assignment.getUser());

        return new ApiResponse<>(
                200,
                true,
                "Invite declined successfully."
        );
    }

    @Override
    public ApiResponse<List<AssignmentMemberResponse>> getMembers(Long assignmentId) {
        Users currentUser = authUtil.getAuthenticatedUser();

        Assignments assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> notFound("Assignment not found."));

        if (!assignment.getUser().getId().equals(currentUser.getId())) {
            throw forbidden("You do not have access to this resource.");
        }

        List<AssignmentMemberResponse> memberResponses = assignmentMemberRepository
                .findByAssignmentIdAndStatus(assignmentId, AssignmentMemberStatus.ACCEPTED)
                .stream()
                .map(m -> AssignmentMemberResponse.builder()
                        .id(m.getId())
                        .userId(m.getUser().getId())
                        .fullname(m.getUser().getFullname())
                        .email(m.getUser().getEmail())
                        .status(m.getStatus())
                        .build())
                .toList();

        return new ApiResponse<>(
                200,
                true,
                "Get all members successfully",
                memberResponses
        );
    }
}
