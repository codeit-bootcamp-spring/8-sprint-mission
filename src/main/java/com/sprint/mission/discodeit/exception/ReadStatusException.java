package com.sprint.mission.discodeit.exception;

import java.util.Map;
import java.util.UUID;

public abstract class ReadStatusException extends DiscodeitException {

  protected ReadStatusException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

  public static class ReadStatusNotFoundException extends ReadStatusException {

    public ReadStatusNotFoundException(UUID readStatusId) {
      super(ErrorCode.READ_STATUS_NOT_FOUND, Map.of("readStatusId", readStatusId));
    }
  }

  public static class ReadStatusAlreadyExists extends ReadStatusException {

    public ReadStatusAlreadyExists(UUID userId, UUID channelId) {
      super(ErrorCode.READ_STATUS_ALREADY_EXISTS, Map.of("userId", userId, "channelId", channelId));
    }
  }
}
