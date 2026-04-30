package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.service.Sse.SseService;
import com.sprint.mission.discodeit.service.auth.DiscodeitUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sse")
public class SseController {

    private final SseService sseService;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> connect(
            @AuthenticationPrincipal DiscodeitUserDetails principal,
            @RequestParam(value = "lastEventId", required = false) String lastEventStrId
    ) {
        UUID receiverId = principal.getUserDto().id();
        log.debug("[SseController] SSE 연결 요청 - receiverId: {}, lastEventId: {}", receiverId, lastEventStrId);

        UUID lastEventId = null;
        if (lastEventStrId != null && !lastEventStrId.isEmpty()) {
            lastEventId = UUID.fromString(lastEventStrId);
        }

        try {
            SseEmitter emitter = sseService.connect(receiverId, lastEventId);
            log.debug("[SseController] SSE 연결 완료 - receiverId: {}", receiverId);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(emitter);
        } catch (Exception e) {
            log.error("[SseController] SSE 연결 처리 중 오류 발생 - receiverId = {}, lastEventId = {}",
                    receiverId, lastEventId, e);
            throw new DiscodeitException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
