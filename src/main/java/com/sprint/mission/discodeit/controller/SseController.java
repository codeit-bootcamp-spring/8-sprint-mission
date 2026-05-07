package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.enums.AuthErrorCode;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.SseService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sse")
public class SseController {

  private final SseService sseService;

  /**
   * 클라이언트 SSE 연결 요청 처리 메서드
   *
   * @param userDetails 현재 인증된 사용자 정보
   * @param lastEventId 클라이언트가 마지막으로 수신한 이벤트 ID(유실 복원용)
   * @return SseEmitter 객체
   */
  @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE) // SSE 스트림 반환 명시
  public ResponseEntity<SseEmitter> connect(
      @AuthenticationPrincipal UserDetails userDetails,
      // 클라이언트가 마지막으로 수신한 이벤트 ID를 헤더에 담아서 전달
      @RequestHeader(value = "Last-Event-ID", required = false) UUID lastEventId) {

    // 실제 사용자 UUID 가져오기
    if (!(userDetails instanceof DiscodeitUserDetails discodeitUserDetails)) {
      log.error("AuthenticationPrincipal이 DiscodeitUserDetials 타입이 아닙니다.");
      throw new DiscodeitException(AuthErrorCode.INVALID_USER_TYPE);
    }
    UUID receiverId = discodeitUserDetails.getUserResponse().id();

    log.info("SSE 연결 요청: receiverId={}, lastEventId={}", receiverId, lastEventId);

    SseEmitter emitter = sseService.connect(receiverId, lastEventId);

    return ResponseEntity.ok(emitter);
  }
}
