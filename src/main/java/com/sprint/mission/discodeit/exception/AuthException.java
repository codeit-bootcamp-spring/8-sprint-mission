package com.sprint.mission.discodeit.exception;

import java.util.Map;

public class AuthException extends DiscodeitException {

  public AuthException(Map<String, Object> details) {
    super(ErrorCode.INVALID_CREDENTIALS, details);
  }

}
