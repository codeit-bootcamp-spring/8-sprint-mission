package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.MessageService;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class MessageWebSocketController {

  private final MessageService messageService;

  @MessageMapping("/messages")
  public void sendMessage(MessageCreateRequest request, Principal principal) {
    UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) principal;
    DiscodeitUserDetails userDetails = (DiscodeitUserDetails) auth.getPrincipal();
    UUID authorId = userDetails.getUserDto().id();
    messageService.create(request, authorId, List.of());
  }
}
