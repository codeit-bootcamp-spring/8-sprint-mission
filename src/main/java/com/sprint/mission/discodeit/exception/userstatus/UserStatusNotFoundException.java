package com.sprint.mission.discodeit.exception.userstatus;

import com.sprint.mission.discodeit.exception.global.ErrorCode;
import java.util.Map;
import java.util.UUID;

/**
 * 조회 시도한 사용자 상태를 찾을 수 없을 때 사용.
 */
public class UserStatusNotFoundException extends UserStatusException {

  public UserStatusNotFoundException() {
    super(ErrorCode.USER_STATUS_NOT_FOUND);
  }

  public UserStatusNotFoundException(UUID userStatusId) {
    super(ErrorCode.USER_STATUS_NOT_FOUND, Map.of("userStatusId", userStatusId));
  }

  public UserStatusNotFoundException(Map<String, Object> details) {
    super(ErrorCode.USER_STATUS_NOT_FOUND, details);
  }

  public static UserStatusNotFoundException withId(UUID userStatusId) {
    return new UserStatusNotFoundException(userStatusId);
  }
}
