package com.sprint.mission.discodeit.global.sse.repository;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class SseEmitterRepository
{
    private final ConcurrentMap<UUID, List<SseEmitter>> data = new ConcurrentHashMap<>();

    public void save(UUID receiverId, SseEmitter sseEmitter) {
        data.computeIfAbsent(receiverId, k -> new CopyOnWriteArrayList<>()).add(sseEmitter);
    }

    public List<SseEmitter> load(UUID receiverId) {
        List<SseEmitter> sseEmitters = data.get(receiverId);

        return sseEmitters == null ? new ArrayList<>() : sseEmitters;
    }

    public void remove(UUID receiverId, SseEmitter sseEmitter) {
        data.computeIfPresent(receiverId, (key, emitters) -> {
            emitters.remove(sseEmitter);
            return emitters.isEmpty() ? null : emitters;
        });
    }

    public ConcurrentMap<UUID, List<SseEmitter>> findAll() {
        return data;
    }

    public void removeAll(UUID receiverId) {
        data.remove(receiverId);
    }
}

