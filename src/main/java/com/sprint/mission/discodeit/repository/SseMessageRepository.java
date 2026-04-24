package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.dto.SseMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.springframework.stereotype.Repository;

@Repository
public class SseMessageRepository {

  // 메시지의 순서를 담고 있는 큐
  private final ConcurrentLinkedDeque<UUID> eventIdQueue = new ConcurrentLinkedDeque<>();

  private final Map<UUID, SseMessage> messages = new ConcurrentHashMap<>();

  public void save(SseMessage message) {
    messages.put(message.id(), message);
    eventIdQueue.add(message.id());
  }

  public List<SseMessage> findAllAfter(UUID lastEventId) {
    // 메시지들 조회
    List<UUID> messageIds = new ArrayList<>(eventIdQueue);

    // lastEventId의 인덱스 조회
    int index = messageIds.indexOf(lastEventId);
    if (index < 0) {
      return List.of();
    }

    return messageIds.subList(index + 1, messageIds.size())
        .stream()
        .map(messages::get)
        .toList();
  }
}
