package com.studentlife.StudentLifeAPIs.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresenceEventResponse {
    private Long         assignmentId;
    private int          onlineCount;
    private java.util.Set<Long> onlineUserIds;
}