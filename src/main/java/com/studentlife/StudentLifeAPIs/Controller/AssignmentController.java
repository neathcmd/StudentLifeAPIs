package com.studentlife.StudentLifeAPIs.Controller;

import com.studentlife.StudentLifeAPIs.DTO.Request.AssignmentRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.InviteRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.UpdateProgressRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.AssignmentMemberResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.AssignmentResponse;
import com.studentlife.StudentLifeAPIs.Scheduler.AssignmentReminderScheduler;
import com.studentlife.StudentLifeAPIs.Service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final AssignmentReminderScheduler assignmentReminderScheduler;

    @PostMapping
    public ResponseEntity<ApiResponse<AssignmentResponse>> create(
            @Valid @RequestBody AssignmentRequest request
            ) {
        return ResponseEntity
                .status(201)
                .body(assignmentService.createAssignment(request));
    }

    @GetMapping("/my-assignment")
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> getMyAssignment() {
        return ResponseEntity.ok(assignmentService.getMyAssignments());
    }

//    @PostMapping("/test-reminder")
//    public ResponseEntity<?> testReminder() {
//        assignmentReminderScheduler.sendReminders();
//        return ResponseEntity.ok("Reminders sent — check your email");
//    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AssignmentResponse>> getById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(assignmentService.getAssignmentById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AssignmentResponse>> updateAssignment(
            @PathVariable Long id,
            @Valid @RequestBody AssignmentRequest request
    ) {
        return ResponseEntity.ok(assignmentService.updateAssignment(id, request));
    }

    @PatchMapping("/{id}/progress")
    public ResponseEntity<ApiResponse<AssignmentResponse>> updateProgress(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProgressRequest request
    ) {
        return ResponseEntity.ok(assignmentService.updateProgress(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> delete(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(assignmentService.deleteAssignment(id));
    }

    @PostMapping("/{assignmentId}/invite")
    public ResponseEntity<ApiResponse<?>> invite(
            @PathVariable Long assignmentId,
            @Valid @RequestBody InviteRequest request
    ) {
        return ResponseEntity.ok(assignmentService.inviteUser(assignmentId, request));
    }

    @PostMapping("/{assignmentId}/accept")
    public ResponseEntity<ApiResponse<?>> accept(
            @PathVariable Long assignmentId
    ) {
        return ResponseEntity.ok(assignmentService.acceptInvite(assignmentId));
    }

    @PostMapping("/{assignmentId}/decline")
    public ResponseEntity<ApiResponse<?>> decline(
            @PathVariable Long assignmentId
    ) {
        return ResponseEntity.ok(assignmentService.declineInvite(assignmentId));
    }

    @GetMapping("/{assignmentId}/members")
    public ResponseEntity<ApiResponse<List<AssignmentMemberResponse>>> getMembers(
            @PathVariable Long assignmentId
    ) {
        return ResponseEntity.ok(assignmentService.getMembers(assignmentId));
    }
}
