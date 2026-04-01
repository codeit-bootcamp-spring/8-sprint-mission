package com.sprint.mission.discodeit.auth.security;

import com.sprint.mission.discodeit.auth.service.DiscodeitUserDetails;
import com.sprint.mission.discodeit.repository.MessageRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("ownershipSecurityExpression")
@RequiredArgsConstructor
public class OwnershipSecurityExpression {

  private final MessageRepository messageRepository;

  public boolean isSelf(UUID targetUserId) {
    UUID currentUserId = currentUserId();
    return currentUserId != null && currentUserId.equals(targetUserId);
  }

  public boolean isMessageAuthor(UUID messageId) {
    if (!messageRepository.existsById(messageId)) {
      return true;
    }

    UUID currentUserId = currentUserId();
    return currentUserId != null && messageRepository.existsByIdAndAuthorId(messageId, currentUserId);
  }

  private UUID currentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return null;
    }

    Object principal = authentication.getPrincipal();
    if (principal instanceof DiscodeitUserDetails userDetails) {
      return userDetails.getUser().getId();
    }
    return null;
  }
}

