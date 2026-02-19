package com.sprint.mission.discodeit.exception;

import java.util.Map;

public class UserStatusException extends DiscodeitException {

  public UserStatusException(Map<String, Object> details) {
    super(ErrorCode.RESOURCE_NOT_FOUND, details);
  }

}
