package com.sprint.mission.discodeit.global.sse.repository;

import com.sprint.mission.discodeit.global.sse.dto.SseMessage;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Repository
public class SseMessageRepository
{
    private static final int MAX_SIZE = 100;

    private final ConcurrentLinkedDeque<UUID> eventIdQueue = new ConcurrentLinkedDeque<>();
    private final Map<UUID, SseMessage> messages = new ConcurrentHashMap<>();

    public void save(SseMessage sseMessage) {
        eventIdQueue.add(sseMessage.id());
        messages.put(sseMessage.id(), sseMessage);

        while (eventIdQueue.size() > MAX_SIZE) {
            UUID oldest = eventIdQueue.pollFirst();

            if (oldest != null) messages.remove(oldest);
        }
    }

    public List<SseMessage> getAfter(UUID lastEventId) {
        boolean found = false;

        List<SseMessage> result = new ArrayList<>();

        if (lastEventId != null) {
            for (UUID eventId : eventIdQueue) {
                if (found) {
                    result.add(messages.get(eventId));
                } else if (eventId.equals(lastEventId)) {
                    found = true;
                }
            }
        }

        return result;
    }
}
