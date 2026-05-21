package com.sprint.mission.discodeit.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Repository;

@Repository
public class SseMessageRepository {

	private static final int MAX_MESSAGES = 2000;

	private final ConcurrentLinkedDeque<UUID> eventIdQueue = new ConcurrentLinkedDeque<>();
	private final ConcurrentMap<UUID, SseMessage> messages = new ConcurrentHashMap<>();

	/**
	 * 이벤트를 저장하고 생성된 ID를 반환합니다. broadcast 이면 {@code receiverIds} 는 무시됩니다.
	 */
	public UUID save(String eventName, String dataJson, boolean broadcast, Collection<UUID> receiverIds) {
		UUID id = UUID.randomUUID();
		Set<UUID> targets = broadcast ? Set.of() : Set.copyOf(receiverIds);
		messages.put(id, new SseMessage(id, eventName, dataJson, broadcast, targets));
		eventIdQueue.addLast(id);
		trimOverflow();
		return id;
	}

	/**
	 * Last-Event-ID 이후(미포함) 수신자에게 해당하는 저장 메시지를 큐 순서대로 반환합니다.
	 * {@code lastEventId} 가 버퍼에 없으면 버퍼 전체 중 해당 수신자용 메시지를 반환합니다.
	 */
	public List<SseMessage> findReplaySequence(UUID lastEventId, UUID receiverId) {
		if (lastEventId == null) {
			return List.of();
		}
		boolean lastInBuffer = queueContains(lastEventId);
		List<SseMessage> out = new ArrayList<>();
		boolean afterLast = false;
		for (UUID id : eventIdQueue) {
			if (lastInBuffer) {
				if (!afterLast) {
					if (id.equals(lastEventId)) {
						afterLast = true;
					}
					continue;
				}
			}
			SseMessage message = messages.get(id);
			if (message != null && appliesToReceiver(message, receiverId)) {
				out.add(message);
			}
		}
		return List.copyOf(out);
	}

	private boolean queueContains(UUID lastEventId) {
		for (UUID id : eventIdQueue) {
			if (id.equals(lastEventId)) {
				return true;
			}
		}
		return false;
	}

	private static boolean appliesToReceiver(SseMessage message, UUID receiverId) {
		return message.broadcast() || message.receiverIds().contains(receiverId);
	}

	private void trimOverflow() {
		while (eventIdQueue.size() > MAX_MESSAGES) {
			UUID removed = eventIdQueue.pollFirst();
			if (removed != null) {
				messages.remove(removed);
			}
		}
	}
}
