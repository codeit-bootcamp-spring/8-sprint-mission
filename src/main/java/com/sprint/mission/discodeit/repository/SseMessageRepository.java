package com.sprint.mission.discodeit.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.springframework.stereotype.Repository;

@Repository
public class SseMessageRepository {

  // 최근 이벤트 ID 기억용 Queue
  private final ConcurrentLinkedDeque<UUID> eventIdQueue = new ConcurrentLinkedDeque<>();

  // 이벤트 ID를 키로 이벤트 데이터 저장
  private final Map<UUID, SseMessage> messages = new ConcurrentHashMap<>();

  // 캐시할 최대 메시지 개수 설정
  private static final int MAX_CACHE_SIZE = 1000;

  public void save(UUID eventId, String eventName, Object data, UUID receiverId) {
    eventIdQueue.addLast(eventId);
    messages.put(eventId, new SseMessage(eventId, eventName, data, receiverId));

    // 큐 크기가 제한을 넘어가면 가장 오래된 이벤트 제거
    while (eventIdQueue.size() > MAX_CACHE_SIZE) {
      UUID oldestEventId = eventIdQueue.pollFirst();
      if (oldestEventId != null) {
        messages.remove(oldestEventId);
      }
    }
  }

  public SseMessage findById(UUID eventId) {
    return messages.get(eventId);
  }

  // 특정 이벤트 ID 이후에 발생한 모든 메시지를 반환하는 메서드
  public List<SseMessage> findAllAfter(UUID lastEventId, UUID receiverId) {
    List<SseMessage> missedMessages = new ArrayList<>();
    boolean found = false;

    // 큐에 담긴 이벤트 ID 순서대로 순회
    for (UUID eventId : eventIdQueue) {
      if (found) {
        // lastEventId 이후의 이벤트들만 수집
        SseMessage message = messages.get(eventId);
        if (message != null && (message.receiverId() == null || message.receiverId()
            .equals(receiverId))) {
          missedMessages.add(message);
        }
      } else if (eventId.equals(lastEventId)) {
        // 클라이언트가 마지막으로 받은 이벤트를 찾음
        found = true;
      }
    }
    return missedMessages;
  }

  // 내부 레코드로 메모리에 저장할 이벤트 데이터 구조. 이벤트 유실 복원 용도
  public record SseMessage(UUID id, String name, Object data, UUID receiverId) {

  }
}
