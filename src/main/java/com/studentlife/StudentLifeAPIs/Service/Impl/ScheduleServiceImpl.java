package com.studentlife.StudentLifeAPIs.Service.Impl;

import com.studentlife.StudentLifeAPIs.DTO.Request.*;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.PaginatedResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.ScheduleResponse;
import com.studentlife.StudentLifeAPIs.Entity.Schedules;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Mapper.ScheduleMapper;
import com.studentlife.StudentLifeAPIs.Repository.ScheduleRepository;
import com.studentlife.StudentLifeAPIs.Repository.UserRepository;
import com.studentlife.StudentLifeAPIs.Service.ScheduleService;
import com.studentlife.StudentLifeAPIs.Specification.ScheduleSpecification;
import com.studentlife.StudentLifeAPIs.Utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

import static com.studentlife.StudentLifeAPIs.Exception.ErrorsExceptionFactory.notFound;
import static com.studentlife.StudentLifeAPIs.Exception.ErrorsExceptionFactory.validation;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final AuthUtil authUtil;
    private final ScheduleMapper scheduleMapper;

    @Override
    public ApiResponse<PaginatedResponse<ScheduleResponse>> getByUserId(
            Long userId,
            int page,
            int size,
            ScheduleFilter filter
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Specification<Schedules> spec = ScheduleSpecification.withFilter(userId, filter);
        Page<Schedules> schedulePage  = scheduleRepository.findAll(spec, pageable);

        if (schedulePage.isEmpty()) {
            throw notFound("No schedule data found for this user.");
        }

        List<ScheduleResponse> responses = schedulePage.getContent()
                .stream()
                .map(scheduleMapper::toResponse)
                .toList();

        PaginatedResponse.PaginationMeta meta =
                new PaginatedResponse.PaginationMeta(
                        schedulePage.getNumber(),
                        schedulePage.getSize(),
                        schedulePage.getTotalElements(),
                        schedulePage.getTotalPages(),
                        schedulePage.hasNext(),
                        schedulePage.hasPrevious()
                );

        PaginatedResponse<ScheduleResponse> paginatedResponse =
                new PaginatedResponse<>(responses, meta);

        return new ApiResponse<>(
                200,
                true,
                "Retrieve schedule successfully.",
                paginatedResponse);
    }

    @Override
    public ApiResponse<ScheduleResponse> getById(Long scheduleId) {

        Schedules schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> notFound("Schedule data not found."));

        return new ApiResponse<>(
                200,
                true,
                "Get schedule successfully.",
                scheduleMapper.toResponse(schedule)
        );
    }

    @Override
    public ApiResponse<ScheduleResponse> createOneTime(OneTimeScheduleRequest request) {

        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw validation("Start time must be before end time");
        }

        Users user = authUtil.getAuthenticatedUser();

        Schedules schedule = scheduleMapper.toEntityFromOneTime(request);
        schedule.setUser(user);
        scheduleRepository.save(schedule);

        return new ApiResponse<>(
                201,
                true,
                "Create schedule successfully.",
                scheduleMapper.toResponse(schedule)
        );
    }

    @Override
    public ApiResponse<ScheduleResponse> createRecurring(RecurringScheduleRequest request) {

        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw validation("Start time must be before end time.");
        }

        Users user = authUtil.getAuthenticatedUser();

        Schedules schedule = scheduleMapper.toEntityFromRecurring(request);
        schedule.setUser(user);
        scheduleRepository.save(schedule);

        return new ApiResponse<>(
                201,
                true,
                "Create schedule successfully.",
                scheduleMapper.toResponse(schedule)
        );
    }

    @Override
    public ApiResponse<ScheduleResponse> updateSchedule(Long scheduleId, ScheduleUpdateRequest request) {
        return null;
    }
}
