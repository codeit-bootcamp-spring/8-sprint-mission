package com.sprint.mission.discodeit.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class SseEmitterRepository {

	private final ConcurrentMap<UUID, List<SseEmitter>> data = new ConcurrentHashMap<>();

	public void save(UUID receiverId, SseEmitter emitter) {
		data.computeIfAbsent(receiverId, k -> Collections.synchronizedList(new ArrayList<>()))
				.add(emitter);
	}

	public void deleteByReceiverId(UUID receiverId, SseEmitter emitter) {
		List<SseEmitter> list = data.get(receiverId);
		if (list == null) {
			return;
		}
		synchronized (list) {
			list.remove(emitter);
			if (list.isEmpty()) {
				data.remove(receiverId, list);
			}
		}
	}

	public List<SseEmitter> findByReceiverId(UUID receiverId) {
		List<SseEmitter> list = data.get(receiverId);
		if (list == null || list.isEmpty()) {
			return List.of();
		}
		synchronized (list) {
			return List.copyOf(list);
		}
	}

	public Iterable<Map.Entry<UUID, List<SseEmitter>>> entries() {
		return data.entrySet();
	}
}
