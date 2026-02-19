package com.sprint.mission.discodeit.exception.user;

import com.sprint.mission.discodeit.exception.global.ErrorCode;
import java.util.Map;

/**
 * 이미 존재하는 사용자(이메일/사용자명 중복)로 등록·수정 시도할 때 사용.
 * details에 email, username 등을 담을 수 있다.
 */
public class UserAlreadyExistsException extends UserException {

  public UserAlreadyExistsException() {
    super(ErrorCode.DUPLICATE_USER);
  }

  public UserAlreadyExistsException(String email, String username) {
    super(ErrorCode.DUPLICATE_USER, Map.of("email", email, "username", username));
  }

  public UserAlreadyExistsException(Map<String, Object> details) {
    super(ErrorCode.DUPLICATE_USER, details);
  }
}
