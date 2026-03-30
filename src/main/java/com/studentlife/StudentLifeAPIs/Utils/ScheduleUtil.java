package com.studentlife.StudentLifeAPIs.Utils;

import com.studentlife.StudentLifeAPIs.DTO.Request.ScheduleUpdateRequest;
import com.studentlife.StudentLifeAPIs.Entity.Schedules;
import com.studentlife.StudentLifeAPIs.Repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static com.studentlife.StudentLifeAPIs.Exception.ErrorsExceptionFactory.*;
import static com.studentlife.StudentLifeAPIs.Exception.ErrorsExceptionFactory.validation;

@Component
@RequiredArgsConstructor
public class ScheduleUtil {

    private final ScheduleRepository scheduleRepository;
    private final AuthUtil authUtil;

    public Schedules findScheduleAndCheckOwnership(Long scheduleId, Long userId) {
        Schedules schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> notFound("Schedule not found."));

        if (!schedule.getUser().getId().equals(userId)) {
            throw forbidden("You are not allowed to perform this action.");
        }

        return schedule;
    }

    public void updateOneTimeFields(Schedules schedule, ScheduleUpdateRequest request) {
        LocalDateTime start = request.getStartTime() != null
                ? request.getStartTime()
                : schedule.getStartTime();
        LocalDateTime end = request.getEndTime() != null
                ? request.getEndTime()
                : schedule.getEndTime();

        if (start.isAfter(end)) {
            throw validation("Start time must be before end time.");
        }

        schedule.setStartTime(start);
        schedule.setEndTime(end);
    }

    public void updateRecurringFields(Schedules schedule, ScheduleUpdateRequest request) {
        LocalTime start = request.getRecurringStartTime() != null
                ? request.getRecurringStartTime()
                : schedule.getRecurringStartTime();
        LocalTime end = request.getRecurringEndTime() != null
                ? request.getRecurringEndTime()
                : schedule.getRecurringEndTime();

        if (start == null || end == null) {
            throw validation("Recurring start time and end time are required.");
        }
        if (start.isAfter(end)) {
            throw validation("Start time must be before end time.");
        }

        schedule.setRecurringStartTime(start);
        schedule.setRecurringEndTime(end);

        if (request.getDayOfWeek() != null) {
            schedule.setDayOfWeek(request.getDayOfWeek());
        }
    }

}
