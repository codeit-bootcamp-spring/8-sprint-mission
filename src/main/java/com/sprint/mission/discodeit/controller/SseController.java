package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.SseService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/sse")
public class SseController {

	private final SseService sseService;

	@GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter connect(
			@AuthenticationPrincipal DiscodeitUserDetails userDetails,
			@RequestParam(value = "lastEventId", required = false) UUID lastEventIdParam,
			@RequestHeader(value = "Last-Event-ID", required = false) String lastEventIdHeader) {
		UUID receiverId = userDetails.getUserDto().id();
		UUID lastEventId = lastEventIdParam;
		if (lastEventId == null && lastEventIdHeader != null && !lastEventIdHeader.isBlank()) {
			try {
				lastEventId = UUID.fromString(lastEventIdHeader);
			} catch (IllegalArgumentException ex) {
				log.debug("Last-Event-ID 를 UUID 로 파싱할 수 없어 무시합니다: {}", lastEventIdHeader);
			}
		}
		return sseService.connect(receiverId, lastEventId);
	}
}
