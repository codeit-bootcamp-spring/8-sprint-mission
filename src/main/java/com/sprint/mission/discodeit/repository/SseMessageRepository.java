package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.dto.data.SseMessage;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.springframework.stereotype.Repository;

@Repository
public class SseMessageRepository {

  private final ConcurrentLinkedDeque<UUID> eventIdQueue = new ConcurrentLinkedDeque<>();
  private final Map<UUID, SseMessage> messages = new ConcurrentHashMap<>();
  private static final int MAX_MESSAGES = 10000;

  public UUID save(String eventName, Object data) {
    UUID eventId = UUID.randomUUID();
    SseMessage sseMessage = new SseMessage(eventId, eventName, data, Instant.now());
    messages.put(eventId, sseMessage);
    eventIdQueue.addLast(eventId);

    if (eventIdQueue.size() > MAX_MESSAGES) {
      UUID oldId = eventIdQueue.pollFirst();
      messages.remove(oldId);
    }
    return eventId;
  }

  public List<SseMessage> findAllAfter(UUID lastEventId) {
    List<SseMessage> result = new ArrayList<>();
    boolean found = false;

    for (UUID eventId : eventIdQueue) {
      if (found) {
        SseMessage sseMessage = messages.get(eventId);
        if (sseMessage != null) {
          result.add(sseMessage);
        }
      }
      if (eventId.equals(lastEventId)) {
        found = true;
      }
    }
    return result;
  }

  public SseMessage findById(UUID eventId) {
    return messages.get(eventId);
  }

  public void clean(Instant before) {
    while (!eventIdQueue.isEmpty()) {
      UUID eventId = eventIdQueue.peekFirst();
      if (eventId == null) {
        break;
      }

      SseMessage sseMessage = messages.get(eventId);
      if (sseMessage != null && sseMessage.createdAt().isBefore(before)) {
        eventIdQueue.pollFirst();
        messages.remove(eventId);
      } else {
        break;
      }
    }

  }

}
