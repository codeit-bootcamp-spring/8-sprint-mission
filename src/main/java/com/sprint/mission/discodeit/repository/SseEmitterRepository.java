package com.sprint.mission.discodeit.repository;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class SseEmitterRepository {
    private final ConcurrentMap<UUID, List<SseEmitter>> data = new ConcurrentHashMap<>();

    public void save(UUID receiverId, SseEmitter emitter) {
        data.computeIfAbsent(receiverId, k -> new CopyOnWriteArrayList<>()).add(emitter);
    }

    public void delete(UUID receiverId, SseEmitter emitter) {
        List<SseEmitter> emitters = data.get(receiverId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                data.remove(receiverId);
            }
        }
    }

    public List<SseEmitter> findAllByReceiverId(UUID receiverId) {
        return data.getOrDefault(receiverId, Collections.emptyList());
    }

    public Map<UUID, List<SseEmitter>> findAll() {
        return data;
    }
}