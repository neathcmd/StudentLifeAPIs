package com.studentlife.StudentLifeAPIs.Service;

import com.studentlife.StudentLifeAPIs.DTO.Request.ScheduleCreateRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.PaginatedResponse;

public interface ScheduleService {

    PaginatedResponse<ApiResponse<?>> getAllByUser(int page, int size);

    ApiResponse<?> createSchedule(ScheduleCreateRequest request);
}
