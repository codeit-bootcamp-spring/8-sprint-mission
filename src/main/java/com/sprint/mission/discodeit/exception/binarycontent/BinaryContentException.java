package com.sprint.mission.discodeit.exception.binarycontent;

import com.sprint.mission.discodeit.exception.global.DiscodeitException;
import com.sprint.mission.discodeit.exception.global.ErrorCode;
import java.util.Map;

public class BinaryContentException extends DiscodeitException {

  public BinaryContentException(ErrorCode errorCode) {
    super(errorCode);
  }

  public BinaryContentException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
