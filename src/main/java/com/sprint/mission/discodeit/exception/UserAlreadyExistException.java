package com.sprint.mission.discodeit.exception;

import java.util.Map;

public class UserAlreadyExistException extends UserException {

  public UserAlreadyExistException(Map<String, Object> details) {
    super(ErrorCode.DUPLICATE_USER, details);
  }

}
