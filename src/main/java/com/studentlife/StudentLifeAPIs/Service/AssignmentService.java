package com.studentlife.StudentLifeAPIs.Service;

import com.studentlife.StudentLifeAPIs.DTO.Request.CreateAssignmentRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.AssignmentResponse;

import java.util.List;

public interface AssignmentService {

    ApiResponse<AssignmentResponse> createAssignment(CreateAssignmentRequest request);

    ApiResponse<List<AssignmentResponse>> getMyAssignments();

    ApiResponse<AssignmentResponse> getAssignmentById(Long id);
}
