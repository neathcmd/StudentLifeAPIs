package com.studentlife.StudentLifeAPIs.DTO.Response;

import com.studentlife.StudentLifeAPIs.Enum.ScheduleType;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

///**
// * One unified response for both ONE_TIME and RECURRING schedules.
// * Fields that don't apply to a type will simply be null.
// *
// * ONE_TIME  → startTime, endTime are set. recurringStartTime, recurringEndTime, dayOfWeek are null.
// * RECURRING → dayOfWeek, recurringStartTime, recurringEndTime are set. startTime, endTime are null.
// */
@Data
public class ScheduleResponse {

    private Long id;
    private String title;
    private String description;
    private ScheduleType type;

    // ONE_TIME fields
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // RECURRING fields
    private Integer dayOfWeek;           // 0=Sun, 1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat
    private LocalTime recurringStartTime;
    private LocalTime recurringEndTime;

    private String location;
    private boolean isImportant;
    private UserSummaryResponse createdBy;
}