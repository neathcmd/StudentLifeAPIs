package com.studentlife.StudentLifeAPIs.Service.Impl;

import com.studentlife.StudentLifeAPIs.Service.OneSignalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OneSignalServiceImpl implements OneSignalService {

    @Value("${onesignal.app-id}")
    private String appId;

    @Value("${onesignal.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void sendPushToUser(String playerId, String title, String message) {
        if (playerId == null || playerId.isBlank()) return;;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = Map.of(
                    "app_id", appId,
                    "include_player_ids", List.of(playerId),
                    "headings", Map.of("en", title),
                    "contents", Map.of("en", message)
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            restTemplate.postForObject(
                    "https://onesignal.com/api/v1/notifications",
                    request,
                    String.class
            );

            log.info("[OneSignal] Push sent to playerId={}", playerId);
        } catch (Exception e) {
            // Never let OneSignal failure crash the chat flow
            log.warn("[OneSignal] Failed to send push to playerId={} — {}", playerId, e.getMessage());
        }
    }
}
