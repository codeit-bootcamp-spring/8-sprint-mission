package com.sprint.mission.discodeit.repository.Sse;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class SseEmitterRepository {

    private final ConcurrentMap<UUID, List<SseEmitter>> data = new ConcurrentHashMap<>();

    public List<SseEmitter> get(UUID userId) {
        return data.get(userId);
    }

    public void add(UUID userId, SseEmitter emitter) {
        data.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);
    }

    public void remove(UUID userId, SseEmitter emitter) {
        List<SseEmitter> emitters = get(userId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) data.remove(userId);
        }
    }

    public void removeByEmitter(SseEmitter emitter) {
        // 모든 사용자 데이터에서 해당 emitter 삭제
        data.values().forEach(list -> list.remove(emitter));

        //비어있는 Key 정리(메모리 최적화)
        data.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }


    public Collection<List<SseEmitter>> getAllUserEmitters() {
        return data.values();
    }
}
