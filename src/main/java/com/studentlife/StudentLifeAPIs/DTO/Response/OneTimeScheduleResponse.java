package com.studentlife.StudentLifeAPIs.DTO.Response;

import com.studentlife.StudentLifeAPIs.Enum.ScheduleType;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class OneTimeScheduleResponse {

    private Long id;
    private String title;
    private String description;
    private ScheduleType type;

    // One-time
    private LocalDateTime startTime;
    private LocalDateTime endTime;


    // Recurring
    private int dayOfWeek;
    private LocalTime recurringStartTime;
    private LocalTime recurringEndTime;

    private String location;
    private UserSummaryResponse createdBy;
}
