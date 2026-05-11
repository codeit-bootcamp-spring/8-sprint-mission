package com.sprint.mission.discodeit.service.sse;

import com.sprint.mission.discodeit.dto.data.SseMessage;
import com.sprint.mission.discodeit.repository.SseEmitterRepository;
import com.sprint.mission.discodeit.repository.SseMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class SseService {

    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60; // 1시간
    private final SseEmitterRepository sseEmitterRepository;
    private final SseMessageRepository sseMessageRepository;

    public SseEmitter connect(UUID receiverId, UUID lastEventId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        sseEmitterRepository.save(receiverId, emitter);

        // 콜백 설정
        emitter.onCompletion(() -> sseEmitterRepository.delete(receiverId, emitter));
        emitter.onTimeout(() -> sseEmitterRepository.delete(receiverId, emitter));
        emitter.onError(e -> sseEmitterRepository.delete(receiverId, emitter));

        // 1. 초기 연결 ping
        ping(emitter);

        // 2. 이벤트 유실 복원 로직
        if (lastEventId != null) {
            List<SseMessage> missedMessages = sseMessageRepository.findAllAfter(lastEventId);
            for (SseMessage message : missedMessages) {
                // 대상자에게 맞는 메시지만 재전송 (브로드캐스트거나 나에게 온 메시지)
                if (message.receiverId() == null || message.receiverId().equals(receiverId)) {
                    sendToClient(emitter, message.id(), message.name(), message.data());
                }
            }
        }

        return emitter;
    }

    public void send(Collection<UUID> receiverIds, String eventName, Object data) {
        for (UUID receiverId : receiverIds) {
            UUID eventId = UUID.randomUUID();
            SseMessage message = new SseMessage(eventId, eventName, data, receiverId);
            sseMessageRepository.save(eventId, message);

            List<SseEmitter> emitters = sseEmitterRepository.findAllByReceiverId(receiverId);
            for (SseEmitter emitter : emitters) {
                sendToClient(emitter, eventId, eventName, data);
            }
        }
    }

    public void broadcast(String eventName, Object data) {
        UUID eventId = UUID.randomUUID();
        SseMessage message = new SseMessage(eventId, eventName, data, null);
        sseMessageRepository.save(eventId, message);

        sseEmitterRepository.findAll().forEach((receiverId, emitters) -> {
            for (SseEmitter emitter : emitters) {
                sendToClient(emitter, eventId, eventName, data);
            }
        });
    }

    private void sendToClient(SseEmitter emitter, UUID id, String name, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .id(id.toString())
                    .name(name)
                    .data(data));
        } catch (IOException e) {
            // 전송 실패 시 연결이 끊긴 것으로 간주. Cleanup에서 정리되도록 방치하거나 로그만 남김.
            log.debug("SSE 클라이언트 전송 실패", e);
        }
    }

    @Scheduled(fixedDelay = 1000 * 60 * 30) // 30분마다
    public void cleanUp() {
        sseEmitterRepository.findAll().forEach((receiverId, emitters) ->
                emitters.removeIf(emitter -> !ping(emitter))
        );
    }

    private boolean ping(SseEmitter sseEmitter) {
        try {
            sseEmitter.send(SseEmitter.event()
                    .name("ping")
                    .data("keep-alive"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}