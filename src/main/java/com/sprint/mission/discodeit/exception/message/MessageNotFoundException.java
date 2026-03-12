package com.sprint.mission.discodeit.exception.message;

import com.sprint.mission.discodeit.exception.global.ErrorCode;
import java.util.Map;
import java.util.UUID;

/**
 * 조회·수정·삭제 시도한 메시지를 찾을 수 없을 때 사용.
 */
public class MessageNotFoundException extends MessageException {

  public MessageNotFoundException() {
    super(ErrorCode.MESSAGE_NOT_FOUND);
  }

  public MessageNotFoundException(UUID messageId) {
    super(ErrorCode.MESSAGE_NOT_FOUND, Map.of("messageId", messageId));
  }

  public MessageNotFoundException(Map<String, Object> details) {
    super(ErrorCode.MESSAGE_NOT_FOUND, details);
  }

  public static MessageNotFoundException withId(UUID messageId) {
    return new MessageNotFoundException(messageId);
  }
}
