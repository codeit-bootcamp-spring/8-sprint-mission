package com.sprint.mission.discodeit.exception.userstatus;

import com.sprint.mission.discodeit.exception.global.ErrorCode;
import java.util.Map;
import java.util.UUID;

/**
 * 이미 사용자 상태가 존재할 때 사용.
 */
public class DuplicateUserStatusException extends UserStatusException {

  public DuplicateUserStatusException() {
    super(ErrorCode.USER_STATUS_ALREADY_EXISTS);
  }

  public DuplicateUserStatusException(UUID userId) {
    super(ErrorCode.USER_STATUS_ALREADY_EXISTS, Map.of("userId", userId));
  }

  public static DuplicateUserStatusException forUser(UUID userId) {
    return new DuplicateUserStatusException(userId);
  }
}
