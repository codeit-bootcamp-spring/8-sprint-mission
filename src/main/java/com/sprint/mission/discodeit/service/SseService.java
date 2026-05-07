package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.repository.SseEmitterRepository;
import com.sprint.mission.discodeit.repository.SseMessageRepository;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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

  private static final Long DEFAULT_TIMEOUT = 1000L * 60 * 30; // 30분
  private final SseEmitterRepository emitterRepository;
  private final SseMessageRepository messageRepository;

  // SseEmitter 객체 생성
  public SseEmitter connect(UUID receiverId, UUID lastEventId) {

    // Emitter 생성
    SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
    emitterRepository.save(receiverId, emitter);

    // 연결 종료 혹은 에러 시 리포지토리에서 제거되도록 콜백
    emitter.onCompletion(() -> emitterRepository.delete(receiverId, emitter));
    emitter.onTimeout(() -> emitterRepository.delete(receiverId, emitter));
    emitter.onError(e -> emitterRepository.delete(receiverId, emitter));

    // 최초 연결 시 핑 전송
    sendToClient(receiverId, emitter, "ping", "connected.");

    // 유실된 이벤트가 있다면 재전송
    if (lastEventId != null) {
      List<SseMessageRepository.SseMessage> missedMessages = messageRepository.findAllAfter(
          lastEventId, receiverId);
      for (SseMessageRepository.SseMessage missedMessage : missedMessages) {
        sendToClientWithEventId(
            receiverId,
            emitter,
            missedMessage.id(),
            missedMessage.name(),
            missedMessage.data()
        );
      }
    }

    return emitter;
  }

  // 이벤트 발송
  public void send(Collection<UUID> receiverIds, String eventName, Object data) {

    UUID eventId = UUID.randomUUID();

    // 수신자에게 각각 발송
    for (UUID receiverId : receiverIds) {
      // 메모리에 메시지 캐싱 (이벤트 유실 복원용)
      messageRepository.save(eventId, eventName, data, receiverId);

      List<SseEmitter> emitters = emitterRepository.findAllByUserId(receiverId);
      for (SseEmitter emitter : emitters) {
        sendToClientWithEventId(receiverId, emitter, eventId, eventName, data);
      }
    }
  }

  // 이벤트 발송(전체)
  public void broadcast(String eventName, Object data) {

    UUID eventId = UUID.randomUUID();

    // 메모리에 메시지 캐싱 (이벤트 유실 복원용
    messageRepository.save(eventId, eventName, data, null);

    // 모든 연결된 유저에게 발송
    Map<UUID, List<SseEmitter>> allEmitters = emitterRepository.findAll();
    for (Map.Entry<UUID, List<SseEmitter>> entry : allEmitters.entrySet()) {
      UUID receiverId = entry.getKey();
      for (SseEmitter emitter : entry.getValue()) {
        sendToClientWithEventId(receiverId, emitter, eventId, eventName, data);
      }
    }
  }

  // 30분마다 만료된 Emitter 정리, 연결 유지 확인용 Ping 전송
  @Scheduled(fixedDelay = 1000 * 60 * 30)
  public void cleanUp() {
    Map<UUID, List<SseEmitter>> allEmitters = emitterRepository.findAll();
    for (Map.Entry<UUID, List<SseEmitter>> entry : allEmitters.entrySet()) {
      for (SseEmitter emitter : entry.getValue()) {
        if (!ping(emitter)) {
          emitterRepository.delete(entry.getKey(), emitter);
        }
      }
    }
  }

  // 최초 연결 또는 만료 여부 확인 용도 더미 이벤트 전송
  private boolean ping(SseEmitter sseEmitter) {
    try {
      sseEmitter.send(SseEmitter.event().name("ping").data("ping"));
      return true;
    } catch (IOException | IllegalStateException e) {
      return false;
    }
  }

  // 내부 발송 유틸 (이벤트 ID 제외)
  private void sendToClient(UUID receiverId, SseEmitter emitter, String eventName, Object data) {
    try {
      emitter.send(SseEmitter.event().name(eventName).data(data));
    } catch (IOException | IllegalStateException e) {
      emitterRepository.delete(receiverId, emitter);
    }
  }

  // 내부 발송 유틸 (이벤트 ID 포함. 유실 복원용)
  private void sendToClientWithEventId(UUID receiverId, SseEmitter emitter, UUID eventId,
      String eventName, Object data) {
    try {
      emitter.send(SseEmitter.event().id(eventId.toString()).name(eventName).data(data));
    } catch (IOException | IllegalStateException e) {
      emitterRepository.delete(receiverId, emitter);
    }
  }
}
