package com.sprint.mission.discodeit.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class SseEmitterRepository {

  private final ConcurrentMap<UUID, List<SseEmitter>> data = new ConcurrentHashMap<>();

  public List<SseEmitter> findByReceiverId(UUID receiverId) {
    return data.getOrDefault(receiverId, List.of());
  }

  public void add(UUID receiverId, SseEmitter emitter) {
    data.compute(receiverId, (key, emitters) -> {
      List<SseEmitter> next = emitters == null ? new ArrayList<>() : new ArrayList<>(emitters);
      next.add(emitter);
      return next;
    });
  }

  public void remove(UUID receiverId, SseEmitter emitter) {
    data.computeIfPresent(receiverId, (key, emitters) -> {
      List<SseEmitter> next = new ArrayList<>(emitters);
      next.remove(emitter);
      return next;
    });
  }

  public Collection<List<SseEmitter>> findAll() {
    return data.values();
  }
}
