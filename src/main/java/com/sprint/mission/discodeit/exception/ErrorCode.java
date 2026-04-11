package com.sprint.mission.discodeit.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
  USER_NOT_FOUND("User를 찾을 수 없습니다."),
  DUPLICATE_USER("이미 존재하는 User입니다."),
  CHANNEL_NOT_FOUND("Channel을 찾을 수 없습니다."),
  PRIVATE_CHANNEL_UPDATE("PRIVATE 채널은 수정할 수 없습니다."),
  VALIDATION_ERROR("요청 값이 올바르지 않습니다."),
  INTERNAL_ERROR("서버 내부 오류입니다."),
  INVALID_CREDENTIALS("아이디 또는 비밀번호가 올바르지 않습니다."),
  BINARY_CONTENT_NOT_FOUND("BinaryContent를 찾을 수 없습니다."),
  MESSAGE_NOT_FOUND("Message를 찾을 수 없습니다."),
  READ_STATUS_ALREADY_EXISTS("이미 존재하는 ReadStatus 입니다."),
  READ_STATUS_NOT_FOUND("ReadStatus를 찾을 수 없습니다."),
  USER_STATUS_ALREADY_EXISTS("해당 User에 대한 UserStatus가 이미 존재합니다."),
  USER_STATUS_NOT_FOUND("UserStatus를 찾을 수 없습니다."),
  INVALID_TOKEN("유효하지 않은 토큰입니다.");

  private final String message;

  ErrorCode(String message) {
    this.message = message;
  }
}
