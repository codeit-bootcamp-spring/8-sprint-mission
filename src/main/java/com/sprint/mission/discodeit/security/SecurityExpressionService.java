package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.repository.MessageRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("securityExpressionService")
@RequiredArgsConstructor
public class SecurityExpressionService {
    private final MessageRepository messageRepository;

    public boolean isCurrentUser(UUID userId, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof DiscodeitUserDetails details)) {
            return false;
        }
        return userId.equals(details.getUserDto().id());
    }

    public boolean isMessageAuthor(UUID messageId, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof DiscodeitUserDetails details)) {
            return false;
        }
        return messageRepository.findById(messageId)
            .map(message -> message.getAuthor() != null
                && details.getUserDto().id().equals(message.getAuthor().getId()))
            .orElse(false);
    }
}
