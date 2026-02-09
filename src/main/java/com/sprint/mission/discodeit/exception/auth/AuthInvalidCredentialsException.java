package com.sprint.mission.discodeit.exception.auth;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class AuthInvalidCredentialsException extends AuthException {

  public AuthInvalidCredentialsException(String username, String password) {
    super(ErrorCode.INVALID_CREDENTIALS, Map.of("username", username, "password", password));
  }
}
