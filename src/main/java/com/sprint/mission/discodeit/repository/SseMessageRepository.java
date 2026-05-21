package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.dto.sse.SseMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.springframework.stereotype.Repository;

@Repository
public class SseMessageRepository {

    private static final int MAX_SIZE = 1000;

    private final ConcurrentLinkedDeque<UUID> eventIdQueue = new ConcurrentLinkedDeque<>();
    private final Map<UUID, SseMessage> messages = new ConcurrentHashMap<>();

    public UUID save(SseMessage message) {
        if (eventIdQueue.size() >= MAX_SIZE) {
            UUID oldest = eventIdQueue.poll();
            if (oldest != null) {
                messages.remove(oldest);
            }
        }
        messages.put(message.id(), message);
        eventIdQueue.offer(message.id());
        return message.id();
    }

    public List<SseMessage> findAllAfter(UUID lastEventId) {
        boolean found = false;
        List<SseMessage> result = new ArrayList<>();
        for (UUID id : eventIdQueue) {
            if (found) {
                SseMessage msg = messages.get(id);
                if (msg != null) {
                    result.add(msg);
                }
            }
            if (id.equals(lastEventId)) {
                found = true;
            }
        }
        return result;
    }
}
