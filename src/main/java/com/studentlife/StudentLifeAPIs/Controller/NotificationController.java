package com.studentlife.StudentLifeAPIs.Controller;

import com.studentlife.StudentLifeAPIs.DTO.Request.NotificationRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.NotificationResponse;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Enum.NotificationType;
import com.studentlife.StudentLifeAPIs.Service.NotificationService;
import com.studentlife.StudentLifeAPIs.Utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthUtil authUtil;

    /**
     * POST /api/v1/notification/send?type=SYSTEM
     * Sends and saves a notification to the authenticated user
     */
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<NotificationResponse>> send(
            @RequestBody NotificationRequest request,
            @RequestParam NotificationType type
    ) {
        Users currentUser = authUtil.getAuthenticatedUser();
        return ResponseEntity.status(201).body(notificationService.sendNotification(request, type, currentUser));
    }

    /**
     * GET /api/v1/notification/unread
     * Returns unread notifications for a user
     */
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnread(@RequestParam Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(userId));
    }

    /**
     * GET /api/v1/notification/unread/count
     * Returns unread notification count (for badge)
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Long> countUnread(@RequestParam Long userId) {
        return ResponseEntity.ok(notificationService.conutUnread(userId));
    }

    /**
     * PUT /api/v1/notification/mark-all-read
     * Marks all notifications as read for a user
     */
    @PutMapping("/mark-all-read")
    public ResponseEntity<ApiResponse<?>> markAllAsRead(@RequestParam Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(new ApiResponse<>(200, true, "All notifications marked as read.", null));
    }
}