package com.studentlife.StudentLifeAPIs.DTO.Request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

///**
// * All fields are optional — only non-null fields will be applied.
// * Works for both ONE_TIME and RECURRING since the service checks the type first.
// */
@Data
public class ScheduleUpdateRequest {

    private String title;
    private String description;
    private String location;
    private Boolean isImportant;

    // ONE_TIME
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // RECURRING
    @Min(value = 0, message = "Day of week must be between 0 (Sun) and 6 (Sat)")
    @Max(value = 6, message = "Day of week must be between 0 (Sun) and 6 (Sat)")
    private Integer dayOfWeek;
    private LocalTime recurringStartTime;
    private LocalTime recurringEndTime;
}