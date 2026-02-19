package com.sprint.mission.discodeit.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

  // Common
  INVALID_REQUEST(HttpStatus.BAD_REQUEST, "C001", "Invalid request"),
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "Internal server error"),
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "C003", "Unauthorized"),
  INVALID_INPUT(HttpStatus.BAD_REQUEST, "C004", "Invalid Input"),

  // User
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "User를 찾을 수 없습니다."),
  DUPLICATE_USERNAME(HttpStatus.CONFLICT, "U002", "같은 username을 사용하는 User가 이미 존재합니다."),
  DUPLICATE_EMAIL(HttpStatus.CONFLICT, "U003", "같은 email을 사용하는 User가 이미 존재합니다."),

  // UserStatus
  USER_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "US001", "UserStatus를 찾을 수 없음"),
  USER_STATUS_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "US002", "해당 User의 UserStatus를 찾을 수 없음"),

  // Channel
  CHANNEL_NOT_FOUND(HttpStatus.NOT_FOUND, "CH001", "해당하는 channel을 찾을 수 없습니다."),
  PRIVATE_CHANNEL_MODIFICATION_NOT_ALLOWED(HttpStatus.FORBIDDEN, "CH002",
      "Private Channel은 수정할 수 없음"),

  // Message
  MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "Message를 찾을 수 없습니다."),

  // BinaryContent
  FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "F001", "첨부 파일을 찾을 수 없습니다."),
  FILE_PROCESSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "F002", "Error processing file"),

  // ReadStatus
  READ_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "Message 읽음 상태를 찾을 수 없음"),
  READ_STATUS_ALREADY_EXISTS(HttpStatus.CONFLICT, "R002", "이미 읽음 상태가 존재함"),

  // Auth
  AUTH_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "A001", "사용자를 찾을 수 없습니다."),
  INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "A002", "비밀번호가 일치하지 않음");

  private final HttpStatus status;
  private final String code;
  private final String message;
}
