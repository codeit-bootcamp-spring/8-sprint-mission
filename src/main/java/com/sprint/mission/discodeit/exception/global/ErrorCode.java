package com.sprint.mission.discodeit.exception.global;

import lombok.Getter;

@Getter
public enum ErrorCode {

  USER_NOT_FOUND("사용자를 찾을 수 없습니다."),
  DUPLICATE_USER("이미 존재하는 사용자입니다."),
  CHANNEL_NOT_FOUND("채널을 찾을 수 없습니다."),
  PRIVATE_CHANNEL_UPDATE("비공개 채널은 수정할 수 없습니다."),
  MESSAGE_NOT_FOUND("메시지를 찾을 수 없습니다."),
  BINARY_CONTENT_NOT_FOUND("파일을 찾을 수 없습니다."),
  READ_STATUS_NOT_FOUND("읽음 상태를 찾을 수 없습니다."),
  USER_STATUS_NOT_FOUND("사용자 상태를 찾을 수 없습니다."),
  USER_STATUS_ALREADY_EXISTS("이미 사용자 상태가 존재합니다."),
  AUTH_FAILED("인증에 실패했습니다."),
  REQUEST_REQUIRED("필수 요청 정보가 없습니다.");

  private final String message;

  ErrorCode(String message) {
    this.message = message;
  }
}
