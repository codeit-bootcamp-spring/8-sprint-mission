package com.sprint.mission.discodeit.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class SseEmitterRepository {

  private final ConcurrentMap<UUID, List<SseEmitter>> data = new ConcurrentHashMap<>();

  // emitter 저장
  public void save(UUID receiverId, SseEmitter emitter) {
    data.computeIfAbsent(receiverId, k -> new CopyOnWriteArrayList<>()).add(emitter);
  }

  // 사용자의 모든 emitter 삭제
  public void deleteById(UUID receiverId) {
    data.remove(receiverId);
  }

  // 사용자의 특정 emitter 삭제
  public void delete(UUID receiverId, SseEmitter emitter) {
    List<SseEmitter> emitters = data.get(receiverId);

    if (emitters != null) {
      emitters.remove(emitter);

      // 메모리 관리를 위해 map에서도 제거
      if (emitters.isEmpty()) {
        data.remove(receiverId);
      }
    }
  }

  public List<SseEmitter> findAllByReceiverId(UUID receiverId) {
    // null 방지를 위해 default 명시
    return data.getOrDefault(receiverId, new ArrayList<>());
  }

  public List<UUID> findAllReceiverIds() {
    return new ArrayList<>(data.keySet());
  }

  public ConcurrentMap<UUID, List<SseEmitter>> findAll() {
    return data; // 전체 맵을 반환해서 서비스에서 순회할 수 있게 해줌
  }
}
