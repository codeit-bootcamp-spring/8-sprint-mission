package com.sprint.mission.discodeit.repository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class SseEmitterRepository {

  private final ConcurrentMap<UUID, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

  public void save(UUID userId, SseEmitter emitter) {

    // 기존 리스트가 없을 때 새로 생성 (CopyOnWriteArrayList -> 스레드 세이프구조)
    emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);
  }

  public void delete(UUID userId, SseEmitter emitter) {

    List<SseEmitter> userEmitters = emitters.get(userId);
    if (userEmitters != null) {
      userEmitters.remove(emitter);
      if (userEmitters.isEmpty()) {
        emitters.remove(userId);
      }
    }
  }

  public List<SseEmitter> findAllByUserId(UUID userId) {
    return emitters.getOrDefault(userId, new CopyOnWriteArrayList<>());
  }

  public ConcurrentMap<UUID, List<SseEmitter>> findAll() {
    return emitters;
  }
}
