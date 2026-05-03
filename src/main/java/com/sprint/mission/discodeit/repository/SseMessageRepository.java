package com.sprint.mission.discodeit.repository;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.springframework.stereotype.Repository;

@Repository
public class SseMessageRepository {

  private static final int MAX_SIZE = 1000;
  private final ConcurrentLinkedDeque<UUID> eventIdQueue = new ConcurrentLinkedDeque<>();
  private final Map<UUID, Object> messages = new ConcurrentHashMap<>();

  public synchronized void put(UUID eventId, Object data) {
    eventIdQueue.addLast(eventId);
    messages.put(eventId, data);
    while (eventIdQueue.size() > MAX_SIZE) {
      UUID removed = eventIdQueue.pollFirst();
      if (removed != null) {
        messages.remove(removed);
      }
    }
  }

  public synchronized Map<UUID, Object> findAfter(UUID lastEventId) {
    Map<UUID, Object> result = new LinkedHashMap<>();
    boolean collect = lastEventId == null;
    for (UUID eventId : eventIdQueue) {
      if (collect) {
        Object data = messages.get(eventId);
        if (data != null) {
          result.put(eventId, data);
        }
        continue;
      }
      if (eventId.equals(lastEventId)) {
        collect = true;
      }
    }
    return result;
  }
}
