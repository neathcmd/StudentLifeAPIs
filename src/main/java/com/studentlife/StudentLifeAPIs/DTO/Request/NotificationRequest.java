package com.studentlife.StudentLifeAPIs.DTO.Request;

import com.studentlife.StudentLifeAPIs.Enum.NotificationType;
import lombok.Data;

import java.time.Instant;

@Data
public class NotificationRequest {
    private String title;
    private String message;
}
