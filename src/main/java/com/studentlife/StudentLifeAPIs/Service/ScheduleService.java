package com.studentlife.StudentLifeAPIs.Service;

import com.studentlife.StudentLifeAPIs.DTO.Request.ScheduleCreateRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.ScheduleFilter;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.PaginatedResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.ScheduleResponse;

import java.util.List;

public interface ScheduleService {

//    ApiResponse<PaginatedResponse<ScheduleResponse>> getAllSchedule(int page, int size);

    ApiResponse<PaginatedResponse<ScheduleResponse>> getByUserId(
            Long userId,
            int page,
            int size,
            ScheduleFilter filter
    );

    ApiResponse<ScheduleResponse> getById(Long scheduleId);

    ApiResponse<?> createSchedule(ScheduleCreateRequest request);
}
