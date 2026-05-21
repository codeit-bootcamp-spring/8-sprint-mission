package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.SseMessage;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.repository.SseEmitterRepository;
import com.sprint.mission.discodeit.repository.SseMessageRepository;
import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
@Slf4j
public class SseService {

  // 현재 실시간 연결 관리
  private final SseEmitterRepository sseEmitterRepository;

  // 지나간 메시지 기록 관리
  private final SseMessageRepository sseMessageRepository;

  // SseEmitter 객체 생성
  public SseEmitter connect(UUID receiverId, UUID lastEventId) {
    SseEmitter emitter = new SseEmitter(1000L * 60 * 30);
    sseEmitterRepository.save(receiverId, emitter);

    emitter.onCompletion(() -> {
      log.info("[SseService] SSE 연결 종료: {}", receiverId);
      sseEmitterRepository.deleteById(receiverId);
    });
    emitter.onTimeout(() -> {
      log.warn("[SseService] SSE 연결 timeout 발생: {}", receiverId);
      sseEmitterRepository.deleteById(receiverId);
    });
    emitter.onError((e) -> {
      log.error("[SseService] SSE 연결 실패: {}", receiverId);
      sseEmitterRepository.deleteById(receiverId);
    });

    // 지난 메시지 복구
    if (lastEventId != null) {
      List<SseMessage> missedMessage = sseMessageRepository.findAllAfter(lastEventId);

      for (SseMessage sseMessage : missedMessage) {
        try {
          emitter.send(SseEmitter.event()
              .id(sseMessage.id().toString())
              .name(sseMessage.eventName())
              .data(sseMessage.data()));
        } catch (IOException e) {
          log.error("[SseService] 메시지 복구 중 실패: {}", receiverId);
          emitter.completeWithError(e);
          sseEmitterRepository.delete(receiverId, emitter);
          break;
        }
      }
    }

    // 최초 연결 여부 검증
    if (!ping(emitter)) {
      sseEmitterRepository.delete(receiverId, emitter);
      throw new DiscodeitException(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    return emitter;
  }

  // SseEmitter 객체를 통해 이벤트 전송
  public void send(Collection<UUID> receiverIds, String eventName, Object data) {
    SseMessage message = new SseMessage(
        UUID.randomUUID(),
        eventName,
        data,
        Instant.now()
    );

    sseMessageRepository.save(message);

    for (UUID receiverId : receiverIds) {
      List<SseEmitter> emitters = sseEmitterRepository.findAllByReceiverId(receiverId);

      // 해당 사용자의 SSE 연결이 없는 경우 스킵
      if (emitters == null || emitters.isEmpty()) {
        log.warn("[SSE] 유저 {}는 현재 접속 중이 아닙니다. (Emitter 없음)", receiverId);
        continue;
      }

      for (SseEmitter emitter : emitters) {
        try {
          emitter.send(SseEmitter.event()
              .id(message.id().toString())
              .name(eventName)
              .data(data));
        } catch (IOException e) {
          log.error("[SseService] 전송 실패로 인한 emitter 제거: {}", receiverId);
          emitter.completeWithError(e);
          sseEmitterRepository.delete(receiverId, emitter);
        } catch (Exception e) {
          log.error("❌ [SSE] 알 수 없는 오류 발생 (유저: {}): ", receiverId, e);
        }
      }
    }
  }

  // SseEmitter 객체를 통해 이벤트 전송
  // 접속한 모두에게 보내고 싶을 때 사용하는 메서드
  public void broadcast(String eventName, Object data) {
    List<UUID> allReceiverIds = sseEmitterRepository.findAllReceiverIds();
    send(allReceiverIds, eventName, data);
  }

  // 주기적으로 ping을 보내서 만료된 SseEmitter 객체 삭제
  @Scheduled(fixedDelay = 1000 * 15)
  public void cleanUp() {
    ConcurrentMap<UUID, List<SseEmitter>> allData = sseEmitterRepository.findAll();

    allData.forEach((receiverId, emitters) -> {
      emitters.removeIf(emitter -> {
        try {
          emitter.send(SseEmitter.event().name("ping").data("ping"));
          return false; // 성공하면 리스트에 유지
        } catch (IOException e) {
          log.info("[SseService] 핑 실패로 인한 정리: {}", receiverId);
          return true; // 실패하면 리스트에서 삭제
        }
      });

      // 4. 만약 리스트가 완전히 비었다면 메모리 관리를 위해 맵에서 ID 자체를 제거
      if (emitters.isEmpty()) {
        sseEmitterRepository.deleteById(receiverId);
      }
    });
  }

  // 최초 연결 또는 만료 여부를 확인하기 위해 더미 이벤트 전송
  private boolean ping(SseEmitter sseEmitter) {
    try {
      sseEmitter.send(SseEmitter.event()
          .name("ping")
          .data("ping"));

      return true;
    } catch (IOException e) {
      return false;
    }
  }
}
