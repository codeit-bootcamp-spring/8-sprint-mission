package com.sprint.mission.discodeit.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;
import lombok.Getter;

@Getter
// API 응답 데이터의 가독성을 높이고 불필요한 네트워크 대역폭 낭비를 줄이기 위함
// 내용이 없는 필드는 JSON 결과물에서 제외
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ErrorResponse {

  private final Instant timestamp = Instant.now();
  private final int status;
  private final String code;
  private final String message;
  private final String exceptionType;
  private final Map<String, Object> details;

  private ErrorResponse(ErrorCode errorCode, Map<String, Object> details, String exceptionType) {
    this.status = errorCode.getStatus().value();
    this.code = errorCode.getCode();
    this.message = errorCode.getMessage();
    this.details = details;
    this.exceptionType = exceptionType;
  }

  public static ErrorResponse of(DiscodeitException e) {
    return new ErrorResponse(e.getErrorCode(), e.getDetails(), e.getClass().getSimpleName());
  }
}
