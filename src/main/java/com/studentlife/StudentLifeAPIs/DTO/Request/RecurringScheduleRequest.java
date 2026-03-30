package com.studentlife.StudentLifeAPIs.DTO.Request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class RecurringScheduleRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Day of week is required")
    @Min(value = 0, message = "Day of week must be between 0 (Sun) and 6 (Sat)")
    @Max(value = 6, message = "Day of week must be between 0 (Sun) and 6 (Sat)")
    private Integer dayOfWeek;  // 0=Sun, 1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat

    @NotNull(message = "Start time is required")
    private LocalTime recurringStartTime;

    @NotNull(message = "End time is required")
    private LocalTime recurringEndTime;

    private String location;
    private boolean isImportant;
}
