package com.sprint.mission.discodeit.exception;

import java.util.Map;
import java.util.UUID;

public abstract class MessageException extends DiscodeitException {

  protected MessageException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

  public static class MessageNotFoundException extends MessageException {

    public MessageNotFoundException(UUID messageId) {
      super(ErrorCode.MESSAGE_NOT_FOUND, Map.of("messageId", messageId));
    }
  }
}
