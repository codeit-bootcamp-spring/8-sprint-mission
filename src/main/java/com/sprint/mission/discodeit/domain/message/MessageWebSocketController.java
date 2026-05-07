package com.sprint.mission.discodeit.domain.message;

import com.sprint.mission.discodeit.auth.exception.UserMismatchException;
import com.sprint.mission.discodeit.auth.service.DiscodeitUserDetails;
import com.sprint.mission.discodeit.domain.message.dto.domain.MessageDto;
import com.sprint.mission.discodeit.domain.message.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.domain.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class MessageWebSocketController
{
    private final MessageService messageService;

    @MessageMapping("messages")
    public MessageDto sendMessage(@Payload MessageCreateRequest messageCreateRequest, Principal principal) {
        DiscodeitUserDetails userDetails = (DiscodeitUserDetails) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        UUID authenticatedUserId = userDetails.getUserDto().id();

        if (!authenticatedUserId.equals(messageCreateRequest.authorId())) {
            throw new UserMismatchException();
        }

        return messageService.create(messageCreateRequest, List.of());
    }
}
