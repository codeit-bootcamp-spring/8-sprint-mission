package com.sprint.mission.discodeit.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@RequiredArgsConstructor
@ConditionalOnMissingBean(RedisJwtRegistry.class)
public class InMemoryJwtRegistry implements JwtRegistry {

    // <userId, Queue<JwtInformation>>
    private final Map<UUID, Queue<JwtInformation>> origin = new ConcurrentHashMap<>();

    private final JwtProperties jwtProperties;

    @Override
    public void registerJwtInformation(JwtInformation jwtInformation) {
        UUID userId = jwtInformation.getUserDto().id();
        Queue<JwtInformation> queue = origin.computeIfAbsent(userId, k -> new ConcurrentLinkedQueue<>());
        queue.add(jwtInformation);
        while (queue.size() > jwtProperties.maxActiveJwtCount()) {
            queue.poll(); // 가장 오래된 토큰 제거 (동시 로그인 제한)
        }
    }

    @Override
    public void invalidateJwtInformationByUserId(UUID userId) {
        origin.remove(userId);
    }

    @Override
    public boolean hasActiveJwtInformationByUserId(UUID userId) {
        Queue<JwtInformation> queue = origin.get(userId);
        return queue != null && queue.stream().anyMatch(JwtInformation::isActive);
    }

    @Override
    public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
        if (accessToken == null || accessToken.isEmpty()) {
            return false;
        }
        String normalized = accessToken.trim();
        return origin.values().stream()
                .flatMap(Queue::stream)
                .anyMatch(info -> normalized.equals(info.getAccessToken().trim()));
    }

    @Override
    public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
        return origin.values().stream()
                .flatMap(Queue::stream)
                .anyMatch(info -> info.getRefreshToken().equals(refreshToken));
    }

    @Override
    public void rotateJwtInformation(String refreshToken, JwtInformation newJwtInformation) {
        UUID userId = newJwtInformation.getUserDto().id();
        Queue<JwtInformation> queue = origin.get(userId);

        if (queue != null) {
            boolean removed = queue.removeIf(info -> info.getRefreshToken().equals(refreshToken));
            if (removed) {
                queue.add(newJwtInformation);
                while (queue.size() > jwtProperties.maxActiveJwtCount()) {
                    queue.poll();
                }
            }
        }
    }

    @Scheduled(fixedDelay = 1000 * 60 * 5)
    @Override
    public void clearExpiredJwtInformation() {
        origin.values().forEach(queue -> {
            queue.removeIf(JwtInformation::isRefreshTokenExpired);
        });

        // 빈 사용자 큐 제거
        origin.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    @Override
    public void invalidateJwtInformationByRefreshToken(String refreshToken) {
        origin.values().forEach(queue -> queue.removeIf(info -> info.getRefreshToken().equals(refreshToken)));
    }
}
