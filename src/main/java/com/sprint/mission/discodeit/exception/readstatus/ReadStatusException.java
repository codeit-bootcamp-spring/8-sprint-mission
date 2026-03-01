package com.sprint.mission.discodeit.exception.readstatus;

import com.sprint.mission.discodeit.exception.global.DiscodeitException;
import com.sprint.mission.discodeit.exception.global.ErrorCode;
import java.util.Map;

public class ReadStatusException extends DiscodeitException {

  public ReadStatusException(ErrorCode errorCode) {
    super(errorCode);
  }

  public ReadStatusException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
