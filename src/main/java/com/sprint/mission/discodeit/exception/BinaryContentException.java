package com.sprint.mission.discodeit.exception;

import java.util.Map;

public class BinaryContentException extends DiscodeitException {

  public BinaryContentException(Map<String, Object> details) {
    super(ErrorCode.RESOURCE_NOT_FOUND, details);
  }

}
