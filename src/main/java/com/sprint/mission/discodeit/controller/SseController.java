package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.service.sse.SseService;
import com.sprint.mission.discodeit.service.user.DiscodeitUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SseController {

    private final SseService sseService;

    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(
            @RequestHeader(value = "Last-Event-ID", required = false) String lastEventIdStr,
            DiscodeitUserDetails userDetails) {
        UserDto userDto = userDetails.getUserDto();

        UUID receiverId = UUID.fromString(userDto.username());
        UUID lastEventId = null;

        if (lastEventIdStr != null && !lastEventIdStr.isBlank()) {
            try {
                lastEventId = UUID.fromString(lastEventIdStr);
            } catch (IllegalArgumentException e) {
                // 유효하지 않은 UUID는 무시
            }
        }

        return sseService.connect(receiverId, lastEventId);
    }
}