package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.sse.SseMessage;
import com.sprint.mission.discodeit.repository.SseEmitterRepository;
import com.sprint.mission.discodeit.repository.SseMessageRepository;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {

    private final SseEmitterRepository emitterRepository;
    private final SseMessageRepository messageRepository;

    public SseEmitter connect(UUID receiverId, UUID lastEventId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        emitter.onCompletion(() -> emitterRepository.remove(receiverId, emitter));
        emitter.onTimeout(() -> emitterRepository.remove(receiverId, emitter));
        emitter.onError(e -> emitterRepository.remove(receiverId, emitter));

        emitterRepository.put(receiverId, emitter);

        if (lastEventId != null) {
            messageRepository.findAllAfter(lastEventId)
                .forEach(msg -> sendToEmitter(emitter, msg));
        }

        ping(emitter);
        log.info("[SSE] 연결 receiverId={}", receiverId);
        return emitter;
    }

    public void send(Collection<UUID> receiverIds, String eventName, Object data) {
        SseMessage message = new SseMessage(UUID.randomUUID(), eventName, data, Instant.now());
        messageRepository.save(message);

        receiverIds.stream()
            .flatMap(id -> emitterRepository.findAllByUserId(id).stream())
            .forEach(emitter -> sendToEmitter(emitter, message));
    }

    public void broadcast(String eventName, Object data) {
        SseMessage message = new SseMessage(UUID.randomUUID(), eventName, data, Instant.now());
        messageRepository.save(message);

        emitterRepository.findAll().values().stream()
            .flatMap(Collection::stream)
            .forEach(emitter -> sendToEmitter(emitter, message));
    }

    @Scheduled(fixedDelay = 1000 * 60 * 30)
    public void cleanUp() {
        emitterRepository.findAll().forEach((userId, emitters) ->
            emitters.removeIf(emitter -> !ping(emitter))
        );
        emitterRepository.cleanUpEmpty();
        log.debug("[SSE] cleanUp 완료");
    }

    private boolean ping(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event().name("ping").data(""));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void sendToEmitter(SseEmitter emitter, SseMessage message) {
        try {
            emitter.send(SseEmitter.event()
                .id(message.id().toString())
                .name(message.eventName())
                .data(message.data()));
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }
}
