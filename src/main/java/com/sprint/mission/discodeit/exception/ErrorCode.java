package com.sprint.mission.discodeit.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
  // 공통
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

  // 인증 관련
  AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다. 로그인 후 이용해주세요."),
  INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),
  ACCOUNT_DISABLED(HttpStatus.UNAUTHORIZED, "비활성화된 계정입니다."),
  ACCOUNT_LOCKED(HttpStatus.UNAUTHORIZED, "잠긴 계정입니다."),
  LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "로그인에 실패했습니다."),
  UNEXPECTED_PRINCIPAL_TYPE(HttpStatus.INTERNAL_SERVER_ERROR, "인증 객체 타입이 올바르지 않습니다."),

  // 인가 관련
  ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 리소스에 접근할 권한이 없습니다."),

  // User 관련
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
  DUPLICATE_USER(HttpStatus.CONFLICT, "사용자가 이미 존재합니다."),
  DUPLICATE_NAME(HttpStatus.CONFLICT, "사용자의 이름이 이미 존재합니다"),
  DUPLICATE_EMAIL(HttpStatus.CONFLICT, "사용자의 이메일이 이미 존재합니다"),
  INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "유효하지 않은 로그인 정보입니다"),

  // Channel 관련
  CHANNEL_NOT_FOUND(HttpStatus.NOT_FOUND, "채널을 찾을 수 없습니다."),
  PRIVATE_CHANNEL_UPDATE(HttpStatus.FORBIDDEN, "비공개 채널은 수정할 수 없습니다."),

  // Message 관련
  MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "메시지를 찾을 수 없습니다."),

  // BinaryContent 관련
  BINARY_CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "바이너리 컨텐츠를 찾을 수 없습니다."),
  BINARY_CONTENT_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "바이너리 컨텐츠를 저장하는 과정에서 오류가 발생했습니다."),
  BINARY_CONTENT_CONVERT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일을 바이너리 컨텐츠로 변환하는 데 실패했습니다."),

  // UserStatus 관련
  USER_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자의 상태 정보를 찾을 수 없습니다."),
  DUPLICATE_USER_STATUS(HttpStatus.CONFLICT, "사용자의 상태 정보가 이미 존재합니다."),

  // ReadStatus 관련
  READ_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "읽음 상태를 찾을 수 없습니다."),
  DUPLICATE_READ_STATUS(HttpStatus.CONFLICT, "읽음 상태가 이미 존재합니다.");

  private final HttpStatus status;
  private final String message;

  ErrorCode(HttpStatus status, String message) {
    this.status = status;
    this.message = message;
  }
}
