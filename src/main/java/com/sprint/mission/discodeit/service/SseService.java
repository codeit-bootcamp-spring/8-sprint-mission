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
import com.sprint.mission.discodeit.event.kafka.RealtimePushEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
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
	private final ObjectProvider<RealtimePushEventPublisher> realtimePushEventPublisher;

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
		RealtimePushEventPublisher publisher = realtimePushEventPublisher.getIfAvailable();
		if (publisher != null) {
			try {
				publisher.publishSseTargeted(distinct, eventName, json, eventId);
			} catch (JsonProcessingException e) {
				throw new IllegalArgumentException("SSE Kafka 페이로드 직렬화 실패", e);
			}
			return;
		}
		deliverToLocalReceivers(distinct, eventName, json, eventId);
	}

	public void broadcast(String eventName, Object data) {
		String json;
		try {
			json = objectMapper.writeValueAsString(data);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("SSE 페이로드 직렬화 실패", e);
		}
		UUID eventId = sseMessageRepository.save(eventName, json, true, List.of());
		RealtimePushEventPublisher publisher = realtimePushEventPublisher.getIfAvailable();
		if (publisher != null) {
			try {
				publisher.publishSseBroadcast(eventName, json, eventId);
			} catch (JsonProcessingException e) {
				throw new IllegalArgumentException("SSE Kafka 페이로드 직렬화 실패", e);
			}
			return;
		}
		deliverBroadcastToLocalEmitters(eventName, json, eventId);
	}

	/** Kafka 수신 측: 이 JVM에 붙어 있는 SSE 클라이언트로만 전달합니다. */
	public void deliverToLocalReceivers(Collection<UUID> receiverIds, String eventName, String dataJson, UUID eventId) {
		for (UUID receiverId : receiverIds) {
			for (SseEmitter emitter : sseEmitterRepository.findByReceiverId(receiverId)) {
				try {
					deliver(emitter, eventName, dataJson, eventId);
				} catch (IOException | IllegalStateException e) {
					log.debug("SSE 전송 실패, receiverId={}, eventName={}", receiverId, eventName, e);
					sseEmitterRepository.deleteByReceiverId(receiverId, emitter);
				}
			}
		}
	}

	/** Kafka 수신 측: 이 JVM의 모든 SSE 연결로 브로드캐스트합니다. */
	public void deliverBroadcastToLocalEmitters(String eventName, String dataJson, UUID eventId) {
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
					deliver(emitter, eventName, dataJson, eventId);
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
