package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "SSE", description = "Server-Sent Events 실시간 통신 API")
public interface SseApi {

  @Operation(summary = "SSE 스트림 연결", description = "클라이언트와 서버 간의 실시간 이벤트 수신을 위한 SSE 연결을 맺습니다.")
  ResponseEntity<SseEmitter> connect(
      @Parameter(hidden = true) DiscodeitUserDetails principal,
      @Parameter(description = "마지막으로 수신한 이벤트 ID (유실 방지용)", required = false) String lastEventIdString
  );
}