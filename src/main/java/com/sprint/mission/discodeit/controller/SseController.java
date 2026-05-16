package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.SseApi;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.sse.SseServiceInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/sse")
public class SseController implements SseApi {

  private final SseServiceInterface sseService;

  @Override
  @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public ResponseEntity<SseEmitter> connect(
      @AuthenticationPrincipal DiscodeitUserDetails principal,
      @RequestHeader(value = "Last-Event-ID", required = false) String lastEventIdString) {

    UUID receiverId = principal.getUserDto().id();
    UUID lastEventId = null;

    if (lastEventIdString != null && !lastEventIdString.isBlank()) {
      try {
        lastEventId = UUID.fromString(lastEventIdString);
      } catch (IllegalArgumentException e) {
        log.warn("잘못된 Last-Event-ID 헤더 형식: {}", lastEventIdString);
      }
    }

    log.info("SSE 연결 요청: receiverId={}, lastEventId={}", receiverId, lastEventId);

    SseEmitter emitter = sseService.connect(receiverId, lastEventId);

    return ResponseEntity.ok(emitter);
  }
}