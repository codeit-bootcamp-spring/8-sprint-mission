package com.sprint.mission.discodeit.security.jwt.store;

import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Component
public class InMemoryJwtRegistry implements JwtRegistry {

    private final Map<UUID, Queue<JwtInformation>> origin = new ConcurrentHashMap<>();
    private final int maxActiveJwtCount;

    private final JwtTokenProvider jwtTokenProvider;

    public InMemoryJwtRegistry(@Value("${jwt.max-active-count}") int maxActiveJwtCount,
                               JwtTokenProvider jwtTokenProvider) {
        this.maxActiveJwtCount = maxActiveJwtCount;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void registerJwtInformation(JwtInformation jwtInformation) {

        UUID userId = jwtInformation.getUserDto().id();

        // 키에 해당하는 내용이 없으면 새로 Queue<JwtInformation>을 생성
        Queue<JwtInformation> tokens = origin.computeIfAbsent(userId, k -> new ConcurrentLinkedQueue<>());

        tokens.offer(jwtInformation);

        while (tokens.size() > maxActiveJwtCount) {
            tokens.poll();
        }
    }

    @Override
    public void invalidateJwtInformationByUserId(UUID userId) {
        origin.remove(userId);
    }

    @Override
    public boolean hasActiveJwtInformationByUserId(UUID userId) {
        Queue<JwtInformation> tokens = origin.get(userId);
        return tokens != null && !tokens.isEmpty();
    }

    @Override
    public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
        return origin.values().stream()
                .flatMap(Queue::stream)
                .anyMatch(information -> information.getAccessToken().equals(accessToken));
    }

    @Override
    public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
        return origin.values().stream()
                .flatMap(Queue::stream)
                .anyMatch(information -> information.getRefreshToken().equals(refreshToken));
    }

    @Override
    public void rotateJwtInformation(String refreshToken, JwtInformation newJwtInformation) {
        UUID userId = newJwtInformation.getUserDto().id();
        Queue<JwtInformation> tokens = origin.get(userId);

        if (tokens != null) {
            // 기존의 RT를 삭제
            tokens.removeIf(information -> information.getRefreshToken().equals(refreshToken));
            // 새로운 JwtInformation를 추가
            tokens.offer(newJwtInformation);
        }
    }

    @Override
    public void invalidateByRefreshToken(String refreshToken) {
        log.info("[JwtRegistry] Refresh Token 단일 값으로 무효화 처리 시작");

        boolean isRemoved = false;

        // 모든 유저의 토큰 큐를 순회하여 일치하는 Refresh Token을 찾아 제거
        for (Queue<JwtInformation> tokens : origin.values()) {
            boolean removedInQueue = tokens.removeIf(
                    information -> information.getRefreshToken().equals(refreshToken)
            );

            if (removedInQueue) {
                isRemoved = true;
                break;
            }
        }

        // 삭제 후 큐가 비어버린 유저가 있는 경우 맵에서 제거하여 메모리를 확보함.
        if (isRemoved) {
            origin.entrySet().removeIf(entry -> entry.getValue().isEmpty());
            log.info("[JwtRegistry] Refresh Token 무효화 완료");
        } else {
            log.warn("[JwtRegistry] 무효화하려는 Refresh Token을 찾을 수 없습니다.");
        }
    }
}
