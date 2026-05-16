package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.dto.data.SseMessage;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Repository
public class SseMessageRepository {

  private static final int MAX_MESSAGE_COUNT = 1000;

  private final ConcurrentLinkedDeque<UUID> eventIdQueue = new ConcurrentLinkedDeque<>();
  private final Map<UUID, SseMessage> messages = new ConcurrentHashMap<>();

  public void save(UUID eventId, SseMessage message) {
    eventIdQueue.addLast(eventId);
    messages.put(eventId, message);

    if (eventIdQueue.size() > MAX_MESSAGE_COUNT) {
      UUID oldEventId = eventIdQueue.pollFirst();
      if (oldEventId != null) {
        messages.remove(oldEventId);
      }
    }
  }

  public List<SseMessage> findAllAfter(UUID lastEventId) {
    return eventIdQueue.stream()
        .dropWhile(id -> !id.equals(lastEventId))
        .skip(1)
        .map(messages::get)
        .filter(Objects::nonNull)
        .toList();
  }
}