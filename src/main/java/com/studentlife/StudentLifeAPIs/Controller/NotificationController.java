package com.studentlife.StudentLifeAPIs.Controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class NotificationController {

    // Client sends to: /app/test
    // Server broadcasts to: /topic/notifications
    @MessageMapping("/test")
    @SendTo("/topic/notifications")
    public String sendTestNotification(String message) {
        return "Server received: " + message;
    }
}
//```
//
//Then test using a simple HTML page or a tool like [Postman WebSocket](https://learning.postman.com/docs/sending-requests/websocket/websocket/) connecting to:
//        ```
//ws://localhost:8080/ws
