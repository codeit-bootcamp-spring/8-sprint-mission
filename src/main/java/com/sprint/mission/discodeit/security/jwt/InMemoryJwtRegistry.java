package com.sprint.mission.discodeit.security.jwt;

import com.sprint.mission.discodeit.dto.auth.JwtInformation;
import java.time.Instant;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InMemoryJwtRegistry implements JwtRegistry{

  private final Map<UUID, Queue<JwtInformation>> origin = new ConcurrentHashMap<>();
  private final int maxActiveJwtCount = 1; // 최대 동시 로그인 수

  // 로그인 성공 시 JwtInformation을 등록
  // 최대 동시 로그인 수(1)를 제어
  @Override
  public void registerJwtInformation(UUID userId, JwtInformation info) {
    Queue<JwtInformation> queue = origin.computeIfAbsent(userId, k -> new ConcurrentLinkedQueue<>());

    while (queue.size() >= maxActiveJwtCount) {
      JwtInformation removed = queue.poll(); // 가장 오래된 세션 제거
      log.info("동시 로그인 제한: userId={}의 이전 세션을 무효화합니다.", userId);
    }
    queue.add(info);
  }

  // 권한 변경 등 이유로 해당 유저 모든 토큰 즉시 무효화 할 때 사용
  @Override
  public void invalidateJwtInformationByUserId(UUID userId) {
    origin.remove(userId);
  }

  // 로그아웃 시 쿠키의 리프레시 토큰 토대로 해당 세션만 제거
  @Override
  public void invalidateJwtInformationByRefreshToken(String refreshToken) {
    origin.values().forEach(queue ->
          queue.removeIf(info -> info.getRefreshToken().equals(refreshToken))
    );
  }

  // 사용자의 로그인 상태를 판단할 때 활용
  @Override
  public boolean hasActiveJwtInformationByUserId(UUID userId) {
    Queue<JwtInformation> queue = origin.get(userId);
    return queue != null && !queue.isEmpty();
  }

  // 요청 필터에서 서버가 발급해준 유효한 토큰인가 확인
  @Override
  public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
    return origin.values().stream()
        .flatMap(Queue::stream)
        .anyMatch(info -> info.getAccessToken().equals(accessToken));
  }

  // 토큰 재발급 시 유효한 토큰인지 확인할 때 활용
  @Override
  public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
    return origin.values().stream()
        .flatMap(Queue::stream)
        .anyMatch(info -> info.getRefreshToken().equals(refreshToken));
  }

  // 토큰 재발급 시 토큰 로테이션을 수행
  @Override
  public void rotateJwtInformation(UUID userId, String oldRefreshToken, JwtInformation newInfo) {
    Queue<JwtInformation> queue = origin.get(userId);
    if (queue != null) {
      queue.stream()
          .filter(info -> info.getRefreshToken().equals(oldRefreshToken))
          .findFirst()
          .ifPresent(info -> info.rotate(
              newInfo.getAccessToken(),
              newInfo.getRefreshToken(),
              newInfo.getExpiresAt()
          ));
    }
  }

  // 만료된 JwtInformation을 삭제
  // 5분마다 돌며 수명이 다한 토큰 정보를 삭제
  @Scheduled(fixedDelay = 1000 * 60 * 5)
  @Override
  public void clearExpiredJwtInformation() {
    Instant now = Instant.now();
    origin.values().forEach(queue ->
        queue.removeIf(info -> info.getExpiresAt().isBefore(now))
    );
    origin.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    log.debug("만료된 JWT 레지스트리 정보를 정리했습니다.");
  }
}
