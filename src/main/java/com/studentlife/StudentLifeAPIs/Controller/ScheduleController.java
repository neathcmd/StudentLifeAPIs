package com.studentlife.StudentLifeAPIs.Controller;

import com.studentlife.StudentLifeAPIs.DTO.Request.OneTimeScheduleRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.RecurringScheduleRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.ScheduleUpdateRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.Service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

//    /**
//     * GET /api/v1/schedule/my-schedule
//     * GET /api/v1/schedule/my-schedule?startDate=2026-03-24&endDate=2026-03-30   ← weekly
//     * GET /api/v1/schedule/my-schedule?startDate=2026-03-30&endDate=2026-03-30   ← daily
//     * GET /api/v1/schedule/my-schedule?startDate=2026-03-01&endDate=2026-03-31   ← monthly
//     *
//     * No date params → returns ALL schedules for the user (recurring + all one-time)
//     * With date params → returns RECURRING (always) + ONE_TIME that fall in that range
//     */
    @GetMapping("/my-schedule")
    public ResponseEntity<ApiResponse<?>> getMySchedules(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(scheduleService.getMySchedules(startDate, endDate));
    }

//    /**
//     * GET /api/v1/schedule/{scheduleId}
//     * Returns a single schedule. Only the owner can access it.
//     */
    @GetMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<?>> getById(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(scheduleService.getById(scheduleId));
    }

//    /**
//     * POST /api/v1/schedule/one-time
//     * Body: { title, description, startTime, endTime, location, isImportant }
//     */
    @PostMapping("/one-time")
    public ResponseEntity<ApiResponse<?>> createOneTime(
            @RequestBody @Valid OneTimeScheduleRequest request
    ) {
        return ResponseEntity.status(201).body(scheduleService.createOneTime(request));
    }

//    /**
//     * POST /api/v1/schedule/recurring
//     * Body: { title, description, dayOfWeek (0-6), recurringStartTime, recurringEndTime, location, isImportant }
//     */
    @PostMapping("/recurring")
    public ResponseEntity<ApiResponse<?>> createRecurring(
            @RequestBody @Valid RecurringScheduleRequest request
    ) {
        return ResponseEntity.status(201).body(scheduleService.createRecurring(request));
    }

//    /**
//     * PUT /api/v1/schedule/{scheduleId}
//     * All fields optional — only non-null fields are updated.
//     * Only the owner can update.
//     */
    @PutMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<?>> updateSchedule(
            @PathVariable Long scheduleId,
            @RequestBody @Valid ScheduleUpdateRequest request
    ) {
        return ResponseEntity.ok(scheduleService.updateSchedule(scheduleId, request));
    }

//    /**
//     * DELETE /api/v1/schedule/{scheduleId}
//     * Only the owner can delete.
//     */
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<?>> deleteSchedule(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(scheduleService.deleteSchedule(scheduleId));
    }
}