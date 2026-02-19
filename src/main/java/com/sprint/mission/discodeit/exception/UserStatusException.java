package com.sprint.mission.discodeit.exception;

import java.util.Map;
import java.util.UUID;

public abstract class UserStatusException extends DiscodeitException {

  protected UserStatusException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

  public static class UserStatusNotFoundException extends UserStatusException {

    public UserStatusNotFoundException(UUID userStatusId) {
      super(ErrorCode.USER_STATUS_NOT_FOUND, Map.of("userStatusId", userStatusId));
    }
  }

  public static class UserStatusUserNotFoundException extends UserStatusException {

    public UserStatusUserNotFoundException(UUID userId) {
      super(ErrorCode.USER_STATUS_USER_NOT_FOUND, Map.of("userId", userId));
    }
  }
}
