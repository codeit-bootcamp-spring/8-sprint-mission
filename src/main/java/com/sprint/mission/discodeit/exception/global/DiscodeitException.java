package com.sprint.mission.discodeit.exception.global;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import lombok.Getter;

/**
 * 모든 예외의 기본이 되는 커스텀 예외.
 * timestamp, errorCode, details(추가 정보)를 갖는다.
 */
@Getter
public class DiscodeitException extends RuntimeException {

  private final Instant timestamp;
  private final ErrorCode errorCode;
  private final Map<String, Object> details;

  public DiscodeitException(ErrorCode errorCode) {
    this(errorCode, null, null);
  }

  public DiscodeitException(ErrorCode errorCode, Map<String, Object> details) {
    this(errorCode, details, null);
  }

  public DiscodeitException(ErrorCode errorCode, Throwable cause) {
    this(errorCode, null, cause);
  }

  public DiscodeitException(ErrorCode errorCode, Map<String, Object> details, Throwable cause) {
    super(errorCode.getMessage(), cause);
    this.timestamp = Instant.now();
    this.errorCode = errorCode;
    this.details = details != null ? Map.copyOf(details) : Collections.emptyMap();
  }
}
