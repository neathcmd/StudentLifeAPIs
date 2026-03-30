package com.studentlife.StudentLifeAPIs.Service.Impl;

import com.studentlife.StudentLifeAPIs.DTO.Request.OneTimeScheduleRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.RecurringScheduleRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.ScheduleUpdateRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.ScheduleResponse;
import com.studentlife.StudentLifeAPIs.Entity.Schedules;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Enum.ScheduleType;
import com.studentlife.StudentLifeAPIs.Mapper.ScheduleMapper;
import com.studentlife.StudentLifeAPIs.Repository.ScheduleRepository;
import com.studentlife.StudentLifeAPIs.Service.ScheduleService;
import com.studentlife.StudentLifeAPIs.Specification.ScheduleSpecification;
import com.studentlife.StudentLifeAPIs.Utils.AuthUtil;
import com.studentlife.StudentLifeAPIs.Utils.ScheduleUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import static com.studentlife.StudentLifeAPIs.Exception.ErrorsExceptionFactory.*;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleMapper scheduleMapper;
    private final AuthUtil authUtil;
    private final ScheduleUtil scheduleUtil;

    // ── Get all schedules for the current user ────────────────────────────────

    @Override
    public ApiResponse<List<ScheduleResponse>> getMySchedules(LocalDate startDate, LocalDate endDate) {
        Users currentUser = authUtil.getAuthenticatedUser();

        Specification<Schedules> spec = ScheduleSpecification.forUser(
                currentUser.getId(), startDate, endDate
        );

        List<ScheduleResponse> responses = scheduleRepository.findAll(spec)
                .stream()
                .map(scheduleMapper::toResponse)
                .toList();

        return new ApiResponse<>(
                200,
                true,
                "Schedules retrieved successfully.",
                responses
        );
    }

    // ── Get a single schedule by ID ───────────────────────────────────────────

    @Override
    public ApiResponse<ScheduleResponse> getById(Long scheduleId) {
        Users currentUser = authUtil.getAuthenticatedUser();
        Schedules schedule = scheduleUtil.findScheduleAndCheckOwnership(scheduleId, currentUser.getId());

        return new ApiResponse<>(
                200,
                true,
                "Schedule retrieved successfully.",
                scheduleMapper.toResponse(schedule)
        );
    }

    // ── Create one-time schedule ──────────────────────────────────────────────

    @Override
    public ApiResponse<ScheduleResponse> createOneTime(OneTimeScheduleRequest request) {
        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw validation("Start time must be before end time.");
        }

        Users currentUser = authUtil.getAuthenticatedUser();

        Schedules schedule = scheduleMapper.toEntityFromOneTime(request);
        schedule.setUser(currentUser);
        scheduleRepository.save(schedule);

        return new ApiResponse<>(
                201,
                true,
                "Schedule created successfully.",
                scheduleMapper.toResponse(schedule)
        );
    }

    // ── Create recurring schedule ─────────────────────────────────────────────

    @Override
    public ApiResponse<ScheduleResponse> createRecurring(RecurringScheduleRequest request) {
        if (request.getRecurringStartTime().isAfter(request.getRecurringEndTime())) {
            throw validation("Start time must be before end time.");
        }

        Users currentUser = authUtil.getAuthenticatedUser();

        Schedules schedule = scheduleMapper.toEntityFromRecurring(request);
        schedule.setUser(currentUser);
        scheduleRepository.save(schedule);

        return new ApiResponse<>(
                201,
                true,
                "Schedule created successfully.",
                scheduleMapper.toResponse(schedule)
        );
    }

    // ── Update a schedule ─────────────────────────────────────────────────────

    @Override
    public ApiResponse<ScheduleResponse> updateSchedule(
            Long scheduleId,
            ScheduleUpdateRequest request
    ) {
        Users currentUser = authUtil.getAuthenticatedUser();
        Schedules schedule = scheduleUtil.findScheduleAndCheckOwnership(scheduleId, currentUser.getId());

        // Common fields
        if (request.getTitle()       != null) schedule.setTitle(request.getTitle());
        if (request.getDescription() != null) schedule.setDescription(request.getDescription());
        if (request.getLocation()    != null) schedule.setLocation(request.getLocation());
        if (request.getIsImportant() != null) schedule.setImportant(request.getIsImportant());

        // Type-specific fields
        if (schedule.getType() == ScheduleType.ONE_TIME) {
            scheduleUtil.updateOneTimeFields(schedule, request);
        } else {
            scheduleUtil.updateRecurringFields(schedule, request);
        }

        scheduleRepository.save(schedule);

        return new ApiResponse<>(
                200,
                true,
                "Schedule updated successfully.",
                scheduleMapper.toResponse(schedule)
        );
    }

    // ── Delete a schedule ─────────────────────────────────────────────────────

    @Override
    public ApiResponse<?> deleteSchedule(Long scheduleId) {
        Users currentUser = authUtil.getAuthenticatedUser();
        Schedules schedule = scheduleUtil.findScheduleAndCheckOwnership(scheduleId, currentUser.getId());

        scheduleRepository.delete(schedule);

        return new ApiResponse<>(
                200,
                true,
                "Schedule deleted successfully."
        );
    }
}