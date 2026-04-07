package com.studentlife.StudentLifeAPIs.Service;

import com.studentlife.StudentLifeAPIs.DTO.Request.CreateAssignmentRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.InviteRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.UpdateProgressRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.AssignmentMemberResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.AssignmentResponse;

import java.util.List;

public interface AssignmentService {

    ApiResponse<AssignmentResponse> createAssignment(CreateAssignmentRequest request);

    ApiResponse<List<AssignmentResponse>> getMyAssignments();

    ApiResponse<AssignmentResponse> getAssignmentById(Long id);

    ApiResponse<AssignmentResponse> updateProgress(Long id, UpdateProgressRequest request);

    ApiResponse<?> deleteAssignment(Long id);

    ApiResponse<?> inviteUser(Long assignmentId, InviteRequest request);

    ApiResponse<?> acceptInvite(Long assignmentId);

    ApiResponse<?> declineInvite(Long assignmentId);

    ApiResponse<List<AssignmentMemberResponse>> getMembers(Long assignmentId);
}
