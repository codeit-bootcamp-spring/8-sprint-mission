package com.sprint.mission.discodeit.global.sse.service;

import com.sprint.mission.discodeit.global.sse.dto.SseMessage;
import com.sprint.mission.discodeit.global.sse.repository.SseEmitterRepository;
import com.sprint.mission.discodeit.global.sse.repository.SseMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
public class SseService
{
    @Value("${sse.delay}")
    private long delay;

    private final SseEmitterRepository sseEmitterRepository;
    private final SseMessageRepository sseMessageRepository;

    public SseEmitter connect(UUID receiverId, UUID lastEventId) {
        SseEmitter sseEmitter = new SseEmitter(delay);

        sseEmitter.onCompletion(() -> sseEmitterRepository.remove(receiverId, sseEmitter));
        sseEmitter.onTimeout(() -> sseEmitterRepository.remove(receiverId, sseEmitter));
        sseEmitter.onError(e -> sseEmitterRepository.remove(receiverId, sseEmitter));

        sseEmitterRepository.save(receiverId, sseEmitter);

        if (lastEventId != null) {
            sseMessageRepository.getAfter(lastEventId).forEach(sseMessage -> {
                try {
                    sseEmitter.send(SseEmitter.event()
                            .name(sseMessage.eventName())
                            .data(sseMessage.data())
                            .id(sseMessage.id().toString())
                    );
                } catch (IOException e) {
                    log.error("메시지 전송에 실패했습니다.", e);
                }
            });
        } else {
            try {
                sseEmitter.send(SseEmitter.event()
                        .name("ping")
                        .id(UUID.randomUUID().toString())
                        .build());
            } catch (IOException e) {
                log.error("메시지 전송에 실패했습니다.", e);
            }
        }

        return sseEmitter;
    }

    public void send(Collection<UUID> receiverIds, String eventName, Object data) {
        UUID eventId = UUID.randomUUID();

        sseMessageRepository.save(new SseMessage(eventId, eventName, data));

        receiverIds.forEach(receiverId -> {
            List<SseEmitter> load = sseEmitterRepository.load(receiverId);

            if (load != null && !load.isEmpty()) {
                for (SseEmitter sseEmitter: load) {
                    try {
                        sseEmitter.send(SseEmitter.event()
                                .name(eventName)
                                .data(data)
                                .id(eventId.toString())
                                .build()
                        );
                    } catch (IOException e) {
                        log.error("메시지 전송에 실패했습니다.", e);
                    }
                }
            }
        });

    }

    public void broadcast(String eventName, Object data) {
        send(sseEmitterRepository.findAll().keySet(), eventName, data);
    }

    @Scheduled(fixedDelayString = "${sse.delay}")
    public void cleanUp() {
        sseEmitterRepository.findAll().forEach((receiverId, sseEmitters) -> {
            sseEmitters.removeIf(sseEmitter -> !ping(sseEmitter));

            if (sseEmitters.isEmpty()) sseEmitterRepository.removeAll(receiverId);
        });
    }

    private boolean ping(SseEmitter sseEmitter) {
        try {
            sseEmitter.send(SseEmitter.event()
                    .name("ping")
                    .data("heartbeat"));
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
