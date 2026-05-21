package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.data.MessageDto;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.service.MessageService;
import java.util.ArrayList;
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

  @MessageMapping("messages")
  public MessageDto sendMessage(@Payload MessageCreateRequest messageCreateRequest) {
    log.info("텍스트 메시지 생성 요청: request={}", messageCreateRequest);
    MessageDto createdMessage = messageService.create(messageCreateRequest, new ArrayList<>());
    log.debug("텍스트 메시지 생성 응답: {}", createdMessage);
    return createdMessage;
  }
}
