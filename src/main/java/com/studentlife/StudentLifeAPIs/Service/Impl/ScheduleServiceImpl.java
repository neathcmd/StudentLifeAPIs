package com.studentlife.StudentLifeAPIs.Service.Impl;

import com.studentlife.StudentLifeAPIs.DTO.Request.ScheduleCreateRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.PaginatedResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.ScheduleResponse;
import com.studentlife.StudentLifeAPIs.Entity.Schedules;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Mapper.ScheduleMapper;
import com.studentlife.StudentLifeAPIs.Repository.ScheduleRepository;
import com.studentlife.StudentLifeAPIs.Repository.UserRepository;
import com.studentlife.StudentLifeAPIs.Service.ScheduleService;
import com.studentlife.StudentLifeAPIs.Utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public ApiResponse<PaginatedResponse<ScheduleResponse>> getAllSchedule(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Schedules> schedulePage = scheduleRepository.findAll(pageable);

        List<ScheduleResponse> scheduleResponses = schedulePage.getContent()
                .stream()
                .map(scheduleMapper::toResponse)
                .toList();

        PaginatedResponse.PaginationMeta paginationMeta = new PaginatedResponse.PaginationMeta(
                schedulePage.getNumber() + 1,
                schedulePage.getSize(),
                schedulePage.getTotalElements(),
                schedulePage.getTotalPages(),
                schedulePage.hasNext(),
                schedulePage.hasPrevious()
        );

        PaginatedResponse<ScheduleResponse> paginatedResponse =
                new PaginatedResponse<>(scheduleResponses, paginationMeta);

        return new ApiResponse<>(
                200,
                true,
                "Schedules fetched successfully.",
                paginatedResponse
        );
    }

    @Override
    public ApiResponse<?> createSchedule(ScheduleCreateRequest request) {

        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw validation("Start time must be before end time.");
        }

        Long userId = authUtil.getAuthenticatedUser().getId();

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> notFound("User not found."));

        Schedules schedule = scheduleMapper.toScheduleEntityCreate(request);
        schedule.setUser(user);

        scheduleRepository.save(schedule);

        ScheduleResponse scheduleResponse = scheduleMapper.toResponse(schedule);

        return new ApiResponse<>(
                201,
                true,
                "Create Schedule successfully.",
                scheduleResponse
        );
    }
}
