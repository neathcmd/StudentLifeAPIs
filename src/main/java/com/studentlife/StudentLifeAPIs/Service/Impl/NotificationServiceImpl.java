package com.studentlife.StudentLifeAPIs.Service.Impl;

import com.studentlife.StudentLifeAPIs.DTO.Request.NotificationRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.NotificationResponse;
import com.studentlife.StudentLifeAPIs.Entity.Notification;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Enum.NotificationType;
import com.studentlife.StudentLifeAPIs.Mapper.NotificationMapper;
import com.studentlife.StudentLifeAPIs.Repository.NotificationRepository;
import com.studentlife.StudentLifeAPIs.Service.NotificationService;
import com.studentlife.StudentLifeAPIs.Utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private AuthUtil authUtil;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public ApiResponse<NotificationResponse> sendNotification(NotificationRequest request, NotificationType type) {

        Users recipient = authUtil.getAuthenticatedUser();

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setType(type);
        notification.setRead(false);

        Notification saved = notificationRepository.save(notification);
        NotificationResponse response = notificationMapper.toResponse(saved);

//        Push real-time via WebSocket
        sendRealTimeNotification(recipient.getId(), response);

        return new ApiResponse<>(
                200,
                true,
                "Message sent successfully.",
                response
        );
    }


    @Override
    public void sendRealTimeNotification(Long userId, NotificationResponse notification) {
        // Sends to: /queue/notifications/{userId}
        // Only the specific user receives this
        messagingTemplate.convertAndSend(
                "/queue/notifications/" + userId,
                notification
        );
    }

    @Override
    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        return notificationRepository
                .findByRecipientIdAndIsReadFalse(userId)
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Override
    public long conutUnread(Long userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    @Override
    public void markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepository
                .findByRecipientIdAndIsReadFalse(userId);
        notifications.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(notifications);
    }
}
