package com.studentlife.StudentLifeAPIs.Service;

import com.studentlife.StudentLifeAPIs.DTO.Request.ScheduleCreateRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.PaginatedResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.ScheduleResponse;

public interface ScheduleService {

    ApiResponse<PaginatedResponse<ScheduleResponse>> getAllSchedule(int page, int size);

    ApiResponse<?> createSchedule(ScheduleCreateRequest request);
}
