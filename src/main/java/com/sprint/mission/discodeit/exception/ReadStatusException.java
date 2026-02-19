package com.sprint.mission.discodeit.exception;

import java.util.Map;

public class ReadStatusException extends DiscodeitException {

  public ReadStatusException(Map<String, Object> details) {
    super(ErrorCode.RESOURCE_NOT_FOUND, details);
  }


}
