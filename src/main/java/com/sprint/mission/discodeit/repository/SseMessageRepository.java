package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.dto.data.SseMessage;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Repository
public class SseMessageRepository {

    private static final int MAX_MESSAGES = 1000;
    private final ConcurrentLinkedDeque<UUID> eventIdQueue = new ConcurrentLinkedDeque<>();
    private final Map<UUID, SseMessage> messages = new ConcurrentHashMap<>();

    public void save(UUID eventId, SseMessage message) {
        messages.put(eventId, message);
        eventIdQueue.addLast(eventId);

        // 메모리 제한 유지
        if (eventIdQueue.size() > MAX_MESSAGES) {
            UUID oldest = eventIdQueue.pollFirst();
            if (oldest != null) {
                messages.remove(oldest);
            }
        }
    }

    // LastEventId 이후의 메시지 반환
    public List<SseMessage> findAllAfter(UUID lastEventId) {
        List<SseMessage> missedMessages = new ArrayList<>();
        boolean found = false;

        for (UUID id : eventIdQueue) {
            if (found) {
                missedMessages.add(messages.get(id));
            } else if (id.equals(lastEventId)) {
                found = true;
            }
        }
        return missedMessages;
    }
}