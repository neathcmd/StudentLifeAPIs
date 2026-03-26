package com.studentlife.StudentLifeAPIs.Controller;

import com.studentlife.StudentLifeAPIs.DTO.Request.ScheduleCreateRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.ScheduleFilter;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.PaginatedResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.ScheduleResponse;
import com.studentlife.StudentLifeAPIs.Service.ScheduleService;
import com.studentlife.StudentLifeAPIs.Utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final AuthUtil authUtil;

    @GetMapping("/schedule/my-schedule")
    public ResponseEntity<ApiResponse<?>> getSchedulesByUserId(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @ModelAttribute ScheduleFilter filter
    ) {
        Long userId = authUtil.getAuthenticatedUser().getId();
        return ResponseEntity.ok(scheduleService.getByUserId(userId, page, size, filter));
    }

//        ---
//
//        **Example API calls:**
//            ```
//    GET /schedule/my-schedule?page=0&size=10
//    GET /schedule/my-schedule?page=0&size=10&title=math
//    GET /schedule/my-schedule?page=0&size=10&startDate=2025-01-01&endDate=2025-06-30
//    GET /schedule/my-schedule?page=0&size=10&title=bio&startDate=2025-03-01

    @GetMapping("/schedule/{scheduleId}")
    public ResponseEntity<ApiResponse<ScheduleResponse>> getById(
            @PathVariable Long scheduleId
    ) {
        return ResponseEntity.ok(scheduleService.getById(scheduleId));
    }

    @PostMapping("/schedule")
    public ResponseEntity<ApiResponse<?>> createdSchedule( @RequestBody ScheduleCreateRequest request) {
        return ResponseEntity.status(201).body(scheduleService.createSchedule(request));
    }
}
