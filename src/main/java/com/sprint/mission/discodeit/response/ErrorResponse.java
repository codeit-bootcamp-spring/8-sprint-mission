package com.sprint.mission.discodeit.response;

import java.time.Instant;
import java.util.Map;
import lombok.Getter;

/**
 * 일관된 예외 응답 형식. timestamp, code, message, details, exceptionType, status(HTTP 상태코드)를 담는다.
 */
@Getter
public class ErrorResponse {

  private final Instant timestamp;
  private final String code;
  private final String message;
  private final Map<String, Object> details;
  private final String exceptionType;
  private final int status;

  public ErrorResponse(Instant timestamp, String code, String message,
      Map<String, Object> details, String exceptionType, int status) {
    this.timestamp = timestamp;
    this.code = code;
    this.message = message;
    this.details = details;
    this.exceptionType = exceptionType;
    this.status = status;
  }
}
