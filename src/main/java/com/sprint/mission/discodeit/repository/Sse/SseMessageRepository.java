package com.sprint.mission.discodeit.repository.Sse;

import com.sprint.mission.discodeit.dto.dto.SseMessage;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Repository
public class SseMessageRepository {

    private static final int MAX_SIZE = 100;
    private final ConcurrentLinkedDeque<UUID> eventIdQueue = new ConcurrentLinkedDeque<>();
    private final Map<UUID, SseMessage> messages = new ConcurrentHashMap<>();

    public void save(SseMessage message) {
        if (eventIdQueue.size() >= MAX_SIZE) {
            UUID oldId = eventIdQueue.poll();
            if (oldId != null) messages.remove(oldId);
        }
        eventIdQueue.offer(message.id());
        messages.put(message.id(), message);
    }

    public List<SseMessage> restore(UUID receiverId, UUID lastEventId) {
        List<SseMessage> missedMessages = new ArrayList<>();
        boolean found = false;
        for (UUID id : eventIdQueue) {
            if (found) {
                SseMessage msg = messages.get(id);
                if (msg != null) {
                    if (msg.receiverIds() == null || msg.receiverIds().contains(receiverId)) {
                        missedMessages.add(msg);
                    }
                }
            }
            if (id.equals(lastEventId)) found = true;
        }
        return missedMessages;
    }
}
