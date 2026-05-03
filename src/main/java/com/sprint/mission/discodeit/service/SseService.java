package com.sprint.mission.discodeit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.repository.SseEmitterRepository;
import com.sprint.mission.discodeit.repository.SseMessage;
import com.sprint.mission.discodeit.repository.SseMessageRepository;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {

	private static final long EMITTER_TIMEOUT_MS = 0L;

	private final ObjectMapper objectMapper;
	private final SseEmitterRepository sseEmitterRepository;
	private final SseMessageRepository sseMessageRepository;

	public SseEmitter connect(UUID receiverId, UUID lastEventId) {
		if (lastEventId != null) {
			log.debug("SSE 연결, receiverId={}, lastEventId={}", receiverId, lastEventId);
		}
		SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MS);
		Runnable detach = () -> sseEmitterRepository.deleteByReceiverId(receiverId, emitter);
		emitter.onCompletion(detach);
		emitter.onTimeout(detach);
		emitter.onError(e -> detach.run());
		sseEmitterRepository.save(receiverId, emitter);
		if (!ping(emitter)) {
			detach.run();
			throw new IllegalStateException("SSE 연결 초기화 실패");
		}
		for (SseMessage message : sseMessageRepository.findReplaySequence(lastEventId, receiverId)) {
			try {
				sendStored(emitter, message);
			} catch (IOException | IllegalStateException e) {
				log.debug("SSE 재전송 실패, receiverId={}, eventId={}", receiverId, message.id(), e);
				detach.run();
				throw new IllegalStateException("SSE 재전송 실패", e);
			}
		}
		return emitter;
	}

	public void send(Collection<UUID> receiverIds, String eventName, Object data) {
		if (receiverIds == null || receiverIds.isEmpty()) {
			return;
		}
		Collection<UUID> distinct = new HashSet<>(receiverIds);
		String json;
		try {
			json = objectMapper.writeValueAsString(data);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("SSE 페이로드 직렬화 실패", e);
		}
		UUID eventId = sseMessageRepository.save(eventName, json, false, distinct);
		for (UUID receiverId : distinct) {
			for (SseEmitter emitter : sseEmitterRepository.findByReceiverId(receiverId)) {
				try {
					deliver(emitter, eventName, json, eventId);
				} catch (IOException | IllegalStateException e) {
					log.debug("SSE 전송 실패, receiverId={}, eventName={}", receiverId, eventName, e);
					sseEmitterRepository.deleteByReceiverId(receiverId, emitter);
				}
			}
		}
	}

	public void broadcast(String eventName, Object data) {
		String json;
		try {
			json = objectMapper.writeValueAsString(data);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("SSE 페이로드 직렬화 실패", e);
		}
		UUID eventId = sseMessageRepository.save(eventName, json, true, List.of());
		for (Map.Entry<UUID, List<SseEmitter>> entry : sseEmitterRepository.entries()) {
			UUID receiverId = entry.getKey();
			List<SseEmitter> emitters = entry.getValue();
			if (emitters == null || emitters.isEmpty()) {
				continue;
			}
			List<SseEmitter> snapshot;
			synchronized (emitters) {
				snapshot = List.copyOf(emitters);
			}
			for (SseEmitter emitter : snapshot) {
				try {
					deliver(emitter, eventName, json, eventId);
				} catch (IOException | IllegalStateException e) {
					log.debug("SSE broadcast 실패, receiverId={}, eventName={}", receiverId, eventName, e);
					sseEmitterRepository.deleteByReceiverId(receiverId, emitter);
				}
			}
		}
	}

	@Scheduled(fixedDelay = 1000 * 60 * 30)
	public void cleanUp() {
		for (Map.Entry<UUID, List<SseEmitter>> entry : sseEmitterRepository.entries()) {
			UUID receiverId = entry.getKey();
			List<SseEmitter> emitters = entry.getValue();
			if (emitters == null) {
				continue;
			}
			List<SseEmitter> snapshot;
			synchronized (emitters) {
				snapshot = List.copyOf(emitters);
			}
			for (SseEmitter emitter : snapshot) {
				if (!ping(emitter)) {
					sseEmitterRepository.deleteByReceiverId(receiverId, emitter);
				}
			}
		}
		log.debug("SSE 정리 완료");
	}

	private boolean ping(SseEmitter sseEmitter) {
		try {
			sseEmitter.send(SseEmitter.event().name("ping").comment("keep-alive"));
			return true;
		} catch (Exception e) {
			log.trace("SSE ping 실패", e);
			return false;
		}
	}

	private void deliver(SseEmitter emitter, String eventName, String dataJson, UUID eventId)
			throws IOException {
		emitter.send(SseEmitter.event()
				.id(eventId.toString())
				.name(eventName)
				.data(dataJson, MediaType.APPLICATION_JSON));
	}

	private void sendStored(SseEmitter emitter, SseMessage message) throws IOException {
		deliver(emitter, message.eventName(), message.dataJson(), message.id());
	}
}
