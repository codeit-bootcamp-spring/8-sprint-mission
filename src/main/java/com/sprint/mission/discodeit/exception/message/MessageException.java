package com.sprint.mission.discodeit.exception.message;

import com.sprint.mission.discodeit.exception.global.DiscodeitException;
import com.sprint.mission.discodeit.exception.global.ErrorCode;
import java.util.Map;

public class MessageException extends DiscodeitException {

  public MessageException(ErrorCode errorCode) {
    super(errorCode);
  }

  public MessageException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
