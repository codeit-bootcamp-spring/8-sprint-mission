package com.sprint.mission.discodeit.security.jwt;

import com.sprint.mission.discodeit.dto.data.JwtInformation;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InMemoryJwtRegistry implements JwtRegistry {

  private final Map<UUID, Queue<JwtInformation>> origin = new ConcurrentHashMap<>();
  private final int maxActiveJwtCount = 1;
  private final JwtTokenProvider jwtTokenProvider;

  @Override
  public void registerJwtInformation(JwtInformation jwtInformation) {
    Queue<JwtInformation> userQueue = origin.computeIfAbsent(
        jwtInformation.getUserDto().id(),
        k -> new java.util.concurrent.ConcurrentLinkedQueue<>()
    );

    while (userQueue.size() >= maxActiveJwtCount) {
      userQueue.poll();
    }
    userQueue.offer(jwtInformation);
  }

  @Override
  public void invalidateJwtInformationByUserId(UUID userId) {
    origin.remove(userId);
  }

  @Override
  public boolean hasActiveJwtInformationByUserId(UUID userId) {
    return origin.containsKey(userId) && !origin.get(userId).isEmpty();
  }

  @Override
  public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
    return origin.values().stream()
        .flatMap(java.util.Collection::stream)
        .anyMatch(info -> info.getAccessToken().equals(accessToken));
  }

  @Override
  public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
    return origin.values().stream()
        .flatMap(java.util.Collection::stream)
        .anyMatch(info -> info.getRefreshToken().equals(refreshToken));
  }

  @Override
  public void rotateJwtInformation(String refreshToken, JwtInformation newJwtInformation) {
    Queue<JwtInformation> userQueue = origin.get(newJwtInformation.getUserDto().id());
    if (userQueue != null) {
      userQueue.stream()
          .filter(info -> info.getRefreshToken().equals(refreshToken))
          .findFirst()
          .ifPresent(info -> info.rotate(newJwtInformation.getAccessToken(),
              newJwtInformation.getRefreshToken()));
    }
  }

  @Scheduled(fixedDelay = 1000 * 60 * 5)
  @Override
  public void clearExpiredJwtInformation() {
    origin.values().forEach(queue -> {
      queue.removeIf(info -> !jwtTokenProvider.validateRefreshToken(info.getRefreshToken()));
    });
    origin.entrySet().removeIf(entry -> entry.getValue().isEmpty());
  }

  @Override
  public void invalidateJwtInformationByRefreshToken(String refreshToken) {
    //인자로 받은 리프레시 토큰과 일치하는 정보가 있다면 삭제
    origin.values().forEach(queue ->
        queue.removeIf(info -> info.getRefreshToken().equals(refreshToken))
    );
    //apahfl rhksfl
    origin.entrySet().removeIf(entry -> entry.getValue().isEmpty());
  }
}
