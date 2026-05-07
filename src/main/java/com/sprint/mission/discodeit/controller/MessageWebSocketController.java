package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Controller
public class MessageWebSocketController {

  private final MessageService messageService;

  // prefix : /pub
  @MessageMapping("/messages")
  public void sendMessage(MessageCreateRequest request) {

    messageService.create(request);
  }
}
