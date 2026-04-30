package com.sprint.mission.discodeit.service.basic.Sse;

import com.sprint.mission.discodeit.dto.dto.SseMessage;
import com.sprint.mission.discodeit.repository.Sse.SseEmitterRepository;
import com.sprint.mission.discodeit.repository.Sse.SseMessageRepository;
import com.sprint.mission.discodeit.service.Sse.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicSseService implements SseService {

    private final SseEmitterRepository emitterRepository;
    private final SseMessageRepository messageRepository;
    private static final Long DEFAULT_TIMEOUT = 30 * 60 * 1000L;

    @Override
    public SseEmitter connect(UUID receiverId, UUID lastEventId) {
        log.debug("[SseService] SSE 연결 요청: receiverId: {}, lastEventId: {}", receiverId, lastEventId);
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        emitter.onCompletion(() -> {
            log.debug("[SseService] SSE Emitter 완료: receiverId: {}", receiverId);
            emitterRepository.remove(receiverId, emitter);
        });
        emitter.onTimeout(() -> {
            log.debug("[SseService] SSE Emitter 타임아웃: receiverId: {}", receiverId);
            emitterRepository.remove(receiverId, emitter);
        });
        emitter.onError(ex -> {
            log.warn("[SseService] SSE Emitter 에러: receiverId: {}", receiverId, ex);
            emitterRepository.remove(receiverId, emitter);
        });

        ping(emitter);

        emitterRepository.add(receiverId, emitter);
        log.debug("[SseService] SSE Emitter 등록 완료: receiverId: {}", receiverId);

        // 유실된 메시지 복원
        if (lastEventId != null) {
            List<SseMessage> missedMessages = messageRepository.restore(lastEventId);
            for (SseMessage msg : missedMessages) {
                try {
                    emitter.send(SseEmitter.event()
                            .name(msg.eventName())
                            .id(msg.id().toString())
                            .data(msg.data())
                    );
                } catch (IOException e) {
                    emitterRepository.remove(receiverId, emitter);
                    break;
                }
            }
        }
        return emitter;
    }

    @Override
    public void send(Collection<UUID> receiverIds, String eventName, Object data) {

        UUID eventId = UUID.randomUUID();

        SseMessage message = new SseMessage(
                eventId,
                eventName,
                data
        );
        messageRepository.save(message);

        for (UUID receiverId : receiverIds) {
            List<SseEmitter> emitters = emitterRepository.get(receiverId);
            if (emitters == null || emitters.isEmpty()) continue;

            List<SseEmitter> failed = new ArrayList<>();

            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event()
                            .name(eventName)
                            .id(eventId.toString())
                            .data(data));
                } catch (IOException e) {
                    failed.add(emitter);
                }
            }
            // 실패한 emitter 없애기
            if (!failed.isEmpty()) {
                for (SseEmitter failedEmitter : failed) {
                    emitterRepository.removeByEmitter(failedEmitter);
                }
            }
        }
    }

    @Override
    public void broadcast(String eventName, Object data) {

        UUID eventId = UUID.randomUUID();

        SseMessage message = new SseMessage(
                eventId,
                eventName,
                data
        );
        messageRepository.save(message);

        // 실패한 연결 수집 후 일괄 제거
        List<SseEmitter> failed = new ArrayList<>();

        emitterRepository.getAllUserEmitters()
                .stream()
                .flatMap(Collection::stream)
                .forEach(emitter -> {
                    try {
                        emitter.send(SseEmitter.event()
                                .name(eventName)
                                .id(eventId.toString())
                                .data(data)
                        );
                    } catch (IOException e) {
                        failed.add(emitter);
                    }
                });

        if (!failed.isEmpty()) {
            for (SseEmitter failedEmitter : failed) {
                emitterRepository.removeByEmitter(failedEmitter);
            }
        }
    }

    @Scheduled(fixedDelay = 30 * 60 * 1000)
    @Override
    public void cleanUp() {
        List<SseEmitter> failed = new ArrayList<>();

        emitterRepository.getAllUserEmitters()
                .stream()
                .flatMap(Collection::stream)
                .forEach(emitter -> {
                    if (!ping(emitter)) {
                        failed.add(emitter);
                    }
                });

        for (SseEmitter failedEmitter : failed) {
            emitterRepository.removeByEmitter(failedEmitter);
        }
    }

    private boolean ping(SseEmitter emitter) {
        log.debug("[SseService] 핑 호출됨");
        try {
            emitter.send(SseEmitter.event()
                    .name("ping")
                    .data("heartBeat")
            );
            log.debug("[SseService] 핑 성공");
            return true;
        } catch (IOException e) {
            log.error("[SseService] 핑 실패: Emitter 종료. 에러: {}", e.getMessage(), e);
            return false;
        }
    }
}
