package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.service.SseService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sse")
public class SseController {

  private final SseService sseService;

  @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter subscribe(@AuthenticationPrincipal UUID userId,
      @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId){
    UUID lastId = null;
    if(lastEventId != null && !lastEventId.isEmpty()){
      lastId = UUID.fromString(lastEventId);
    }
    return sseService.connect(userId, lastId);
  }
}
