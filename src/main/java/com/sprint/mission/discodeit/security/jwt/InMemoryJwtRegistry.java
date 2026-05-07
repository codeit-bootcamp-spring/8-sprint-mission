package com.sprint.mission.discodeit.security.jwt;

import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
//@Component
public class InMemoryJwtRegistry implements JwtRegistry {

  // 유저ID, 유저가 발급받은 토큰 정보 큐
  private final Map<UUID, Queue<JwtInformation>> jwtInfoMap = new ConcurrentHashMap<>();
  // 최대 동시 로그인
  private final int maxActiveJwtCount;
  private final JwtTokenProvider jwtTokenProvider;

  public InMemoryJwtRegistry(
      JwtTokenProvider jwtTokenProvider,
      @Value("${jwt.max-active-count:1}") int maxActiveJwtCount) {
    this.jwtTokenProvider = jwtTokenProvider;
    this.maxActiveJwtCount = maxActiveJwtCount;
  }

  @Override
  public void registerJwtInformation(JwtInformation jwtInformation) {
    UUID userId = jwtInformation.getUserResponse().id();

    // 유저의 큐를 가져오거나 없으면 새로 생성
    Queue<JwtInformation> queue = jwtInfoMap.computeIfAbsent(userId,
        k -> new ConcurrentLinkedQueue<>());

    // 큐에 새 토큰 정보 주입
    queue.add(jwtInformation);

    // 동시 로그인 제한
    while (queue.size() > maxActiveJwtCount) {
      queue.poll();
      log.info("동시 로그인 제한으로 이전 기기 로그아웃 처리 userId={}", userId);
    }
  }

  @Override
  public void invalidateJwtInformationByUserId(UUID userId) {

    jwtInfoMap.remove(userId);
  }

  @Override
  public boolean hasActiveJwtInformationByUserId(UUID userId) {

    Queue<JwtInformation> queue = jwtInfoMap.get(userId);

    if (queue == null || queue.isEmpty()) {
      return false;
    }

    // Registry에 토큰 만료시각을 JwtInformation에 캐싱 후, 캐싱된 시간으로 검사
    return queue.stream().anyMatch(JwtInformation::isActive);
  }

  @Override
  public boolean hasActiveJwtInformationByAccessToken(String accessToken) {

    return jwtInfoMap.values().stream()
        .flatMap(Collection::stream)
        .anyMatch(info -> info.getAccessToken().equals(accessToken));
  }

  @Override
  public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {

    return jwtInfoMap.values().stream()
        .flatMap(Collection::stream)
        .anyMatch(info -> info.getRefreshToken().equals(refreshToken));
  }

  @Override
  public void rotateJwtInformation(String oldRefreshToken, JwtInformation newJwtInformation) {

    UUID userId = newJwtInformation.getUserResponse().id();
    Queue<JwtInformation> queue = jwtInfoMap.get(userId);

    if (queue != null) {
      queue.stream()
          .filter(info -> info.getRefreshToken().equals(oldRefreshToken))
          .findFirst()
          .ifPresent(info -> info.rotateToken(
              // userResponse 반영하도록 추가
              newJwtInformation.getUserResponse(),
              newJwtInformation.getAccessToken(),
              newJwtInformation.getRefreshToken(),
              newJwtInformation.getAccessTokenExpiry(),
              newJwtInformation.getRefreshTokenExpiry()
          ));
    }
  }

  @Override
  public void invalidateJwtInformationByRefreshToken(String refreshToken) {
    jwtInfoMap.values().forEach(queue ->
        queue.removeIf(info -> info.getRefreshToken().equals(refreshToken)));
  }

  @Override
  @Scheduled(fixedDelay = 1000 * 60 * 5)
  public void clearExpiredJwtInformation() {

    jwtInfoMap.forEach((userId, queue) -> {
      queue.removeIf(info -> !info.isActive());
    });

    jwtInfoMap.entrySet().removeIf(entry -> entry.getValue().isEmpty());
  }
}
