package com.sprint.mission.discodeit.exception.user;

import com.sprint.mission.discodeit.exception.global.ErrorCode;
import java.util.Map;
import java.util.UUID;

/**
 * 조회 시도한 사용자를 찾을 수 없을 때 사용. details에 userId(조회 시도한 사용자 ID) 등을 담을 수 있다.
 */
public class UserNotFoundException extends UserException {

		public UserNotFoundException() {
				super(ErrorCode.USER_NOT_FOUND);
		}

		public UserNotFoundException(UUID userId) {
				super(ErrorCode.USER_NOT_FOUND, Map.of("userId", userId));
		}

		public UserNotFoundException(Map<String, Object> details) {
				super(ErrorCode.USER_NOT_FOUND, details);
		}

  public static UserNotFoundException withId(UUID userId) {
    return new UserNotFoundException(userId);
  }

  public static UserNotFoundException withUsername(String username) {
    return new UserNotFoundException(Map.of("username", username));
  }
}
