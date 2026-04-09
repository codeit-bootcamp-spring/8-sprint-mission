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
    // 객체를 조회하지 않고, DB에서 데이터 존재 여부만 리턴해준다.
    return messageRepository.existsByIdAndAuthorId(messageId, currentUserId);
  }
}
