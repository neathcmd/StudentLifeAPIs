package com.studentlife.StudentLifeAPIs.Service;

import com.studentlife.StudentLifeAPIs.DTO.Request.OneTimeScheduleRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.RecurringScheduleRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.ScheduleUpdateRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.ScheduleResponse;
import com.studentlife.StudentLifeAPIs.Entity.Users;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleService {

    // Get all schedules for the current user, optionally filtered by date range
    ApiResponse<List<ScheduleResponse>> getMySchedules(LocalDate startDate, LocalDate endDate);

    // Get a single schedule by ID (only the owner can access)
    ApiResponse<ScheduleResponse> getById(Long scheduleId);

    // Create a one-time event
    ApiResponse<ScheduleResponse> createOneTime(OneTimeScheduleRequest request);

    // Create a recurring weekly event
    ApiResponse<ScheduleResponse> createRecurring(RecurringScheduleRequest request);

    // Update any schedule (only the owner can update)
    ApiResponse<ScheduleResponse> updateSchedule(Long scheduleId, ScheduleUpdateRequest request);

    // Delete a schedule (only the owner can delete)
    ApiResponse<?> deleteSchedule(Long scheduleId);

    Long createAssignmentSchedule(
            String title, String description,
            LocalDateTime startDate,
            LocalDateTime dueDate,
            Long assignmentId,
            Users user
    );
}