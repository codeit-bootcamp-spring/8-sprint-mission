package com.sprint.mission.discodeit.security.jwt.store;

import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Component
public class InMemoryJwtRegistry implements JwtRegistry {

    private final Map<UUID, Queue<JwtInformation>> origin = new ConcurrentHashMap<>();
    private final Set<String> accessTokenIndexes = ConcurrentHashMap.newKeySet();
    private final Set<String> refreshTokenIndexes = ConcurrentHashMap.newKeySet();

    private final int maxActiveJwtCount;
    private final JwtTokenProvider jwtTokenProvider;

    public InMemoryJwtRegistry(@Value("${jwt.max-active-count}") int maxActiveJwtCount,
                               JwtTokenProvider jwtTokenProvider) {
        this.maxActiveJwtCount = maxActiveJwtCount;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void registerJwtInformation(JwtInformation jwtInformation) {
        origin.compute(jwtInformation.getUserDto().id(), (key, queue) -> {
            if (queue == null) {
                queue = new ConcurrentLinkedQueue<>();
            }

            if (queue.size() >= maxActiveJwtCount) {
                JwtInformation deprecatedJwtInformation = queue.poll();
                if (deprecatedJwtInformation != null) {
                    removeTokenIndex(
                            deprecatedJwtInformation.getAccessToken(),
                            deprecatedJwtInformation.getRefreshToken()
                    );
                }
            }
            queue.add(jwtInformation);
            addTokenIndex(
                    jwtInformation.getAccessToken(),
                    jwtInformation.getRefreshToken()
            );
            return queue;
        });
    }

    @Override
    public void invalidateJwtInformationByUserId(UUID userId) {
        origin.computeIfPresent(userId, (key, queue) -> {
            queue.forEach(jwtInformation -> {
                removeTokenIndex(
                        jwtInformation.getAccessToken(),
                        jwtInformation.getRefreshToken()
                );
            });
            queue.clear();
            return null;
        });
    }

    @Override
    public boolean hasActiveJwtInformationByUserId(UUID userId) {
        return origin.containsKey(userId);
    }

    @Override
    public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
        return accessTokenIndexes.contains(accessToken);
    }

    @Override
    public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
        return refreshTokenIndexes.contains(refreshToken);
    }

    @Override
    public void rotateJwtInformation(String refreshToken, JwtInformation newJwtInformation) {
        origin.computeIfPresent(newJwtInformation.getUserDto().id(), (key, queue) -> {
            queue.stream().filter(jwtInformation -> jwtInformation.getRefreshToken().equals(refreshToken))
                    .findFirst()
                    .ifPresent(jwtInformation -> {
                        removeTokenIndex(jwtInformation.getAccessToken(), jwtInformation.getRefreshToken());
                        jwtInformation.rotate(
                                newJwtInformation.getAccessToken(),
                                newJwtInformation.getRefreshToken()
                        );
                        addTokenIndex(
                                newJwtInformation.getAccessToken(),
                                newJwtInformation.getRefreshToken()
                        );
                    });
            return queue;
        });
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

    @Scheduled(fixedDelay = 1000 * 60 * 5)
    @Override
    public void clearExpiredJwtInformation() {
        origin.entrySet().removeIf(entry -> {
            Queue<JwtInformation> queue = entry.getValue();
            queue.removeIf(jwtInformation -> {
                boolean isExpired =
                        !jwtTokenProvider.validateAccessToken(jwtInformation.getAccessToken()) ||
                                !jwtTokenProvider.validateRefreshToken(jwtInformation.getRefreshToken());
                if (isExpired) {
                    removeTokenIndex(
                            jwtInformation.getAccessToken(),
                            jwtInformation.getRefreshToken()
                    );
                }
                return isExpired;
            });
            // 큐가 비었을 경우 엔트리 삭제
            return queue.isEmpty();
        });
    }

    private void addTokenIndex(String accessToken, String refreshToken) {
        accessTokenIndexes.add(accessToken);
        refreshTokenIndexes.add(refreshToken);
    }

    private void removeTokenIndex(String accessToken, String refreshToken) {
        accessTokenIndexes.remove(accessToken);
        refreshTokenIndexes.remove(refreshToken);
    }

}
