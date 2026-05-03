package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.service.MessageService;
import jakarta.validation.Valid;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MessageWebSocketController {

    private final MessageService messageService;

    /**
     * 첨부 없는 텍스트 메시지 — 클라이언트는 {@code /pub/messages} 로 전송합니다.
     */
    @MessageMapping("/messages")
    public void sendMessage(@Payload @Valid MessageCreateRequest request) {
        log.debug("STOMP 메시지 생성, channelId={}, authorId={}", request.channelId(), request.authorId());
        messageService.create(request, Collections.emptyList());
    }
}
