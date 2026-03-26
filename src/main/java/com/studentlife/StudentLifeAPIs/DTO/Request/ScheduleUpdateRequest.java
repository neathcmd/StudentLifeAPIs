package com.studentlife.StudentLifeAPIs.DTO.Request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
public class ScheduleUpdateRequest {
    private String title;
    private String description;

    // One-time fields
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Recurring fields
    private Integer dayOfWeek;
    private LocalTime recurringStartTime;
    private LocalTime recurringEndTime;

    private String location;
}
