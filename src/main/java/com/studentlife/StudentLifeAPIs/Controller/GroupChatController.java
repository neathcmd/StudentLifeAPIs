package com.studentlife.StudentLifeAPIs.Controller;

import com.studentlife.StudentLifeAPIs.DTO.Request.ChatMessageRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.GroupMessageResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.GroupResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.MemberResponse;
import com.studentlife.StudentLifeAPIs.Service.GroupChatService;
import com.studentlife.StudentLifeAPIs.Service.PresenceService;
import com.studentlife.StudentLifeAPIs.Utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class GroupChatController {

    private final GroupChatService groupChatService;
    private final AuthUtil authUtil;


    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageRequest request, Principal principal) {
        Long senderId = authUtil.getUserIdFromPrincipal(principal);
        groupChatService.sendMessage(request, senderId);
    }

    @MessageMapping("/chat.join")
    public void joinGroup(@Payload ChatMessageRequest request, Principal principal) {
        Long userId = authUtil.getUserIdFromPrincipal(principal);
        groupChatService.userJoined(request.getAssignmentId(), userId, principal.getName());
    }

    @MessageMapping("/chat.leave")
    public void leaveGroup(@Payload ChatMessageRequest request, Principal principal) {
        Long userId = authUtil.getUserIdFromPrincipal(principal);
        groupChatService.userLeft(request.getAssignmentId(), userId, principal.getName());
    }

    @GetMapping("/api/v1/chat/groups")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<GroupResponse>>> getMyGroups() {
        return ResponseEntity.ok(groupChatService.getMyGroups());
    }


    @GetMapping("/api/v1/chat/{assignmentId}/history")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<GroupMessageResponse>>> getHistory(
            @PathVariable Long assignmentId
    ) {
        return ResponseEntity.ok(groupChatService.getChatHistory(assignmentId));
    }

    @GetMapping("/api/v1/{assignmentId}/members")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<MemberResponse>>> getGroupMembers(
            @PathVariable Long assignmentId
    ) {
        return ResponseEntity.ok(groupChatService.getGroupMember(assignmentId));
    }

    @DeleteMapping("/api/v1/chat/{assignmentId}/history")
    @ResponseBody
    public ResponseEntity<ApiResponse<?>> clearHistory(
            @PathVariable Long assignmentId
    ) {
        return ResponseEntity.ok(groupChatService.clearChatHistory(assignmentId));
    }
}