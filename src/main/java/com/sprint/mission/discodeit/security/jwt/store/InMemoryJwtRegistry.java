package com.sprint.mission.discodeit.security.jwt.store;

import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
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

    public InMemoryJwtRegistry(int maxActiveJwtCount, JwtTokenProvider jwtTokenProvider) {
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

    @Scheduled(fixedDelay = 1000 * 60 * 5)
    @Override
    public void clearExpiredJwtInformation() {
        log.info("[JwtRegistry] 만료된 JwtInformation 정리 스케줄러 실행");

        Date now = new Date();

        for (Queue<JwtInformation> tokens : origin.values()) {
            tokens.removeIf(information -> {
                try {
                    Date expirationDate = jwtTokenProvider.getExpiration(information.getAccessToken());

                    return expirationDate.before(now);
                } catch (Exception e) {
                    log.warn("[JwtRegistry] 토큰 파싱 실패, 삭제 처리: {}", e.getMessage());
                    return true;
                }
            });
        }

        // JwtInformation 정보가 없는 경우 Map에서 삭제하여 메모리 확보
        origin.entrySet().removeIf(entry -> entry.getValue().isEmpty());

        log.info("[JwtRegistry] 만료된 JwtInformation 토큰 정리 완료");
    }
}
