package com.sprint.mission.discodeit.exception.user;

import com.sprint.mission.discodeit.exception.global.ErrorCode;

/**
 * 인증 실패(비밀번호 불일치 등)일 때 사용.
 */
public class InvalidCredentialsException extends UserException {

  public InvalidCredentialsException() {
    super(ErrorCode.AUTH_FAILED);
  }

  public static InvalidCredentialsException wrongPassword() {
    return new InvalidCredentialsException();
  }
}
