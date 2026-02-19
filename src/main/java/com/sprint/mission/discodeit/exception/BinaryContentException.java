package com.sprint.mission.discodeit.exception;

import java.util.Map;
import java.util.UUID;

public abstract class BinaryContentException extends DiscodeitException {

  protected BinaryContentException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

  public static class BinaryContentNotFoundException extends BinaryContentException {

    public BinaryContentNotFoundException(UUID binaryContentId) {
      super(ErrorCode.FILE_NOT_FOUND, Map.of("binaryContentId", binaryContentId));
    }
  }
}
