package com.studentlife.StudentLifeAPIs.Service;

import java.util.Set;

public interface PresenceService {

    void userJoined(Long assignmentId, Long userId);

    void userLeft(Long assignmentId, Long userId);

    void userDisconnected(Long userId);

    Set<Long> getOnlineUsers(Long assignmentId);

    int getOnlineCount(Long assignmentId);

    boolean isOnline(Long assignmentId, Long userId);
}
