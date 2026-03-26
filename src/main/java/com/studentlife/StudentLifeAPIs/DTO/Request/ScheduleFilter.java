package com.studentlife.StudentLifeAPIs.DTO.Request;

import lombok.Data;

import java.time.Instant;

@Data
public class ScheduleFilter {
    private String title;
    private Instant startDate;
    private Instant endDate;
}
