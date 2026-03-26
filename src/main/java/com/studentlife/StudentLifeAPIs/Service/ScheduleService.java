package com.studentlife.StudentLifeAPIs.Service;

import com.studentlife.StudentLifeAPIs.DTO.Request.OneTimeScheduleRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.RecurringScheduleRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.ScheduleFilter;
import com.studentlife.StudentLifeAPIs.DTO.Request.ScheduleUpdateRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.PaginatedResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.RecurringScheduleResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.OneTimeScheduleResponse;

public interface ScheduleService {

    ApiResponse<PaginatedResponse<OneTimeScheduleResponse>> getByUserId(
            Long userId,
            int page,
            int size,
            ScheduleFilter filter
    );

    ApiResponse<OneTimeScheduleResponse> getById(Long scheduleId);

    ApiResponse<OneTimeScheduleResponse> createOneTime(OneTimeScheduleRequest request);

    ApiResponse<RecurringScheduleResponse> createRecurring(RecurringScheduleRequest request);

    ApiResponse<OneTimeScheduleResponse> updateSchedule(Long scheduleId, ScheduleUpdateRequest request);

    ApiResponse<?> deleteSchedule(Long scheduleId, Long userId);
}
