package com.sprint.mission.discodeit.exception;

import java.util.Map;

public abstract class AuthException extends DiscodeitException {

  protected AuthException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

  public static class AuthUserNotFound extends AuthException {

    public AuthUserNotFound(String username) {
      super(ErrorCode.AUTH_USER_NOT_FOUND, Map.of("username", username));
    }
  }

  public static class InvalidPassword extends AuthException {

    public InvalidPassword(String password) {
      super(ErrorCode.INVALID_PASSWORD, Map.of("password", password));
    }
  }
}
