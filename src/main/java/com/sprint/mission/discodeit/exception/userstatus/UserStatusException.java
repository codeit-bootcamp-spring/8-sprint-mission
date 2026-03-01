package com.sprint.mission.discodeit.exception.userstatus;

import com.sprint.mission.discodeit.exception.global.DiscodeitException;
import com.sprint.mission.discodeit.exception.global.ErrorCode;
import java.util.Map;

public class UserStatusException extends DiscodeitException {

  public UserStatusException(ErrorCode errorCode) {
    super(errorCode);
  }

  public UserStatusException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
