package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.repository.MessageRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("messageSecurity") // SpEL에서 @messageSecurity로 참조 가능
@RequiredArgsConstructor
public class MessageSecurity {

  private final MessageRepository messageRepository;

  public boolean isAuthor(UUID messageId, UUID currentUserId) {
    return messageRepository.findById(messageId)
        .map(message -> message.getAuthor().getId().equals(currentUserId))
        .orElse(false); // 메시지가 없으면 권한 없음 처리
  }
}
