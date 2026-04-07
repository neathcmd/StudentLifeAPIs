package com.studentlife.StudentLifeAPIs.DTO.Request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateAssignmentRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;         // optional

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotNull(message = "Due date is required")
    @Future(message = "Due date must be in the future")
    private LocalDateTime dueDate;
}