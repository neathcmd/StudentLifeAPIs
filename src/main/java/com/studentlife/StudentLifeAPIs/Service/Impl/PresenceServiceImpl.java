package com.studentlife.StudentLifeAPIs.Service.Impl;

import com.studentlife.StudentLifeAPIs.Service.PresenceService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PresenceServiceImpl implements PresenceService {

    private final Map<Long, Set<Long>> onlineUsers = new ConcurrentHashMap<>();

    @Override
    public void userJoined(Long assignmentId, Long userId) {
        onlineUsers
                .computeIfAbsent(assignmentId, k -> ConcurrentHashMap.newKeySet())
        .add(userId);
    }

    @Override
    public void userLeft(Long assignmentId, Long userId) {
        Set<Long> users = onlineUsers.get(assignmentId);
        if (users != null) {
            users.remove(userId);
            if (users.isEmpty()) onlineUsers.remove(assignmentId);
        }
    }

    @Override
    public void userDisconnected(Long userId) {
        onlineUsers.values().forEach(set -> set.remove(userId));
    }

    @Override
    public Set<Long> getOnlineUsers(Long assignmentId) {
        return onlineUsers.getOrDefault(assignmentId, Collections.emptySet());
    }

    @Override
    public int getOnlineCount(Long assignmentId) {
        return getOnlineUsers(assignmentId).size();
    }

    @Override
    public boolean isOnline(Long assignmentId, Long userId) {
        Set<Long> users = onlineUsers.get(assignmentId);
        return users != null && users.contains(userId);
    }
}
