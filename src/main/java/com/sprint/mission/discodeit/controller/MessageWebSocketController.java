package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.dto.MessageDto;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MessageWebSocketController {

    private static final Logger log = LoggerFactory.getLogger(MessageWebSocketController.class);

    private final MessageService messageService;

    @MessageMapping("/messages")
    public void handleMessage(MessageCreateRequest request) {
        log.info("[MessageWebSocketController] 메시지 웹소켓 전송 요청 - channelId: {}, authorId: {}", request.channelId(), request.authorId());
        MessageDto messageDto = messageService.create(request, List.of());
        log.info("[MessageWebSocketController] 메시지 웹소켓 전송 완료 - ID: {}", messageDto.id());
    }
}
