package com.studentlife.StudentLifeAPIs.Service;

import com.studentlife.StudentLifeAPIs.DTO.Request.OneTimeScheduleRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.RecurringScheduleRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.ScheduleFilter;
import com.studentlife.StudentLifeAPIs.DTO.Request.ScheduleUpdateRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.PaginatedResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.ScheduleResponse;

public interface ScheduleService {

    ApiResponse<PaginatedResponse<ScheduleResponse>> getByUserId(
            Long userId,
            int page,
            int size,
            ScheduleFilter filter
    );

    ApiResponse<ScheduleResponse> getById(Long scheduleId);

    ApiResponse<ScheduleResponse> createOneTime(OneTimeScheduleRequest request);

    ApiResponse<ScheduleResponse> createRecurring(RecurringScheduleRequest request);

    ApiResponse<ScheduleResponse> updateSchedule(Long scheduleId, ScheduleUpdateRequest request);
}
