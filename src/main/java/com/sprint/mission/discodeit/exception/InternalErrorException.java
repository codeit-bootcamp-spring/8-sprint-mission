package com.sprint.mission.discodeit.exception;

import java.util.Map;
import java.util.UUID;

public class InternalErrorException extends DiscodeitException {

  public InternalErrorException(UUID id) {
    super(ErrorCode.INTERNAL_ERROR, Map.of("id", id));
  }
}
