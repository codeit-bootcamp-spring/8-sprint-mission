package com.sprint.mission.discodeit.exception.user;

import com.sprint.mission.discodeit.exception.ErrorCode;

public class InvalidTokenException extends UserException {

  public InvalidTokenException() {
    super(ErrorCode.INVALID_TOKEN);
  }
}
