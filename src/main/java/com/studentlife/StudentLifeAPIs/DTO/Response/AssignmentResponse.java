package com.studentlife.StudentLifeAPIs.DTO.Response;

import com.studentlife.StudentLifeAPIs.Enum.AssignmentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Date;

@Data
@Builder
public class AssignmentResponse {

    private Long id;
    private String title;
    private String description;
    private String subject;
    private Date dueDate;
    private AssignmentStatus status;
    private Integer progress;
    private Instant createdAt;
    private Instant updatedAt;
}