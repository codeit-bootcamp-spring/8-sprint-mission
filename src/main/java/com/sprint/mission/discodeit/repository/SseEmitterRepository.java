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

  public void save(UUID receiverId, SseEmitter sseEmitter) {
    data.computeIfAbsent(receiverId, key -> new ArrayList<>()).add(sseEmitter);
  }

  public List<SseEmitter> findAllReceiverId(UUID receiverId) {
    return data.getOrDefault(receiverId, new ArrayList<>());
  }

  public void delete(UUID receiverId, SseEmitter sseEmitter) {
    List<SseEmitter> emitters = data.get(receiverId);
    if (emitters != null) {
      emitters.remove(sseEmitter);

      if (emitters.isEmpty()) {
        data.remove(receiverId);
      }
    }
  }

  public Collection<UUID> findAllReceiverIds() {
    return data.keySet();
  }

  public List<SseEmitter> findAll() {
    return data.values().stream()
        .flatMap(List::stream)
        .toList();
  }
}
