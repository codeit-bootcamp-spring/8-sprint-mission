package com.sprint.mission.discodeit.exception.enums;

import com.sprint.mission.discodeit.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

  INVALID_CREDENTIALS(7001, "INVALID_CREDENTIALS", HttpStatus.UNAUTHORIZED,
      "사용자명 또는 비밀번호가 올바르지 않습니다."),
  ACCOUNT_DISABLED(7002, "ACCOUNT_DISABLED", HttpStatus.UNAUTHORIZED, "계정이 비활성화 되었습니다."),
  ACCESS_DENIED(7003, "ACCESS_DENIED", HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
  AUTHENTICATION_FAILED(7004, "AUTHENTICATION_FAILED", HttpStatus.UNAUTHORIZED, "인증에 실패했습니다."),
  // JWT 토큰 관련 에러코드
  INVALID_TOKEN(4005, "INVALID_TOKEN", HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
  EXPIRED_TOKEN(4006, "EXPIRED_TOKEN", HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
  INVALID_USER_TYPE(4007, "INVALID_USER_TYPE", HttpStatus.UNAUTHORIZED, "유효하지 않은 사용자 타입입니다.");

  private final int numeric;
  private final String errorKey;
  private final HttpStatus httpStatus;
  private final String message;

  @Override
  public String getDomain() {
    return "AUTH";
  }

  @Override
  public String getCode() {
    return getDomain() + "-" + getErrorKey();
  }
}
