package com.sprint.mission.discodeit.security.jwt;

import com.sprint.mission.discodeit.dto.JwtInformation;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// @Component
public class InMemoryJwtRegistry implements JwtRegistry {

  private static final Logger log = LoggerFactory.getLogger(InMemoryJwtRegistry.class);
  // <userId, Queue<JwtInformation>>
  private final Map<UUID, Queue<JwtInformation>> origin = new ConcurrentHashMap<>();
  private final int maxActiveJwtCount;
  private final JwtTokenProvider jwtTokenProvider;

  public InMemoryJwtRegistry(@Value("${jwt.max-active-count}") int maxActiveJwtCount,
      JwtTokenProvider jwtTokenProvider) {
    this.maxActiveJwtCount = maxActiveJwtCount;
    this.jwtTokenProvider = jwtTokenProvider;
  }

  // 로그인 성공 시 JwtInformation 등록
  @Override
  public void registerJwtInformation(JwtInformation jwtInformation) {
    UUID userId = jwtInformation.userDto().id();

    origin.compute(userId, (key, queue) -> {
      if (queue == null) {
        queue = new ConcurrentLinkedQueue<>();
      }

      while (queue.size() >= maxActiveJwtCount) {
        queue.poll();
      }

      queue.add(jwtInformation);
      return queue;
    });
  }

  // 해당 유저의 모든 JwtInformation 삭제
  @Override
  public void invalidateJwtInformationByUserId(UUID userID) {
    origin.remove(userID);
  }

  // 사용자의 로그인 상태를 판단할 때 활용
  @Override
  public boolean hasActiveJwtInformationByUserId(UUID userID) {
    Queue<JwtInformation> queue = origin.get(userID);
    return queue != null && !queue.isEmpty();
  }

  // 필터에서 유효한 토큰인지 확인할 때 활용
  @Override
  public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
    return origin.values().stream()
        .flatMap(Collection::stream)
        .anyMatch(jwtInformation -> jwtInformation.accessToken().equals(accessToken));
  }

  // 토큰 재발급 시 유효한 토큰인지 확인할 때 활용
  @Override
  public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
    return origin.values().stream()
        .flatMap(Collection::stream)
        .anyMatch(jwtInformation -> jwtInformation.refreshToken().equals(refreshToken));
  }

  // 토큰 재발급 시 토큰 로테이션 수행
  @Override
  public void rotateJwtInformation(String refreshToken, JwtInformation newJwtInformation) {
    UUID userId = newJwtInformation.userDto().id();

    origin.computeIfPresent(userId, (key, queue) -> {
      queue.removeIf(jwt -> jwt.refreshToken().equals(refreshToken));
      queue.add(newJwtInformation);
      return queue;
    });
  }

  // 만료된 JwtInformation 삭제
  @Scheduled(fixedDelay = 1000 * 60 * 5)
  @Override
  public void clearExpiredJwtInformation() {
    origin.forEach((userId, queue) -> {
      queue.removeIf(jwtInformation -> {
        Date expiration = jwtTokenProvider.getExpiration(jwtInformation.refreshToken());

        boolean isExpired = expiration.before(new Date());

        if (isExpired) {
          log.debug("[InMemoryJwtRegistry] 만료된 세션 정리: userId={}, exp={}", userId, expiration);
        }

        return isExpired;
      });

      if (queue.isEmpty()) {
        origin.remove(userId);
      }
    });
  }
}
