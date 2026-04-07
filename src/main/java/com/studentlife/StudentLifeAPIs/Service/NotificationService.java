package com.studentlife.StudentLifeAPIs.Service;

import com.studentlife.StudentLifeAPIs.DTO.Request.NotificationRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.NotificationResponse;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Enum.NotificationType;

import java.util.List;

public interface NotificationService {

    ApiResponse<NotificationResponse> sendNotification(NotificationRequest request, NotificationType type, Users recipient);

    void sendRealTimeNotification(Long userId, NotificationResponse notification);

    List<NotificationResponse> getUnreadNotifications(Long userId);

    long conutUnread(Long userId);

    void markAllAsRead(Long userId);
}
