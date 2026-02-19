package com.sprint.mission.discodeit.exception;

import java.util.Map;
import java.util.UUID;

public abstract class UserException extends DiscodeitException {

  protected UserException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

  public static class DuplicateEmailException extends UserException {

    public DuplicateEmailException(String email) {
      super(ErrorCode.DUPLICATE_EMAIL, Map.of("email", email));
    }
  }

  public static class DuplicateUsernameException extends UserException {

    public DuplicateUsernameException(String username) {
      super(ErrorCode.DUPLICATE_USERNAME, Map.of("username", username));
    }
  }

  public static class UserNotFoundException extends UserException {

    public UserNotFoundException(UUID userId) {
      super(ErrorCode.USER_NOT_FOUND, Map.of("userId", userId));
    }
  }
}
