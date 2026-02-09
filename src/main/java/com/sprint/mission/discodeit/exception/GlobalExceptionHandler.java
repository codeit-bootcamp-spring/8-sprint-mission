package com.sprint.mission.discodeit.exception;

import java.time.Instant;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(DiscodeitException.class)
  public ResponseEntity<ErrorResponse> handleDiscodeit(DiscodeitException e) {

    // ErrorCode 별 status 매핑 전략 -> switch로 매핑
    int status = switch (e.getErrorCode()) {
      case INVALID_CREDENTIALS -> 401;
      case USER_NOT_FOUND, CHANNEL_NOT_FOUND, BINARY_CONTENT_NOT_FOUND, MESSAGE_NOT_FOUND,
           READ_STATUS_NOT_FOUND, USER_STATUS_NOT_FOUND -> 404;
      case DUPLICATE_USER, READ_STATUS_ALREADY_EXISTS, USER_STATUS_ALREADY_EXISTS -> 409;
      case PRIVATE_CHANNEL_UPDATE -> 403;
      default -> 500;
    };

    log.error("[DiscodeitException] code={}, details={}", e.getErrorCode(), e.getDetails(), e);

    ErrorResponse body = new ErrorResponse(
        e.getTimestamp(),
        e.getErrorCode().name(),
        e.getErrorCode().getMessage(),
        e.getDetails(),
        e.getClass().getSimpleName(),
        status
    );

    return ResponseEntity.status(status).body(body);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnknown(Exception e) {
    log.error("[UnhandledException], e");

    ErrorResponse body = new ErrorResponse(
        Instant.now(),
        ErrorCode.INTERNAL_ERROR.name(),
        ErrorCode.INTERNAL_ERROR.getMessage(),
        Map.of(),
        e.getClass().getSimpleName(),
        500
    );
    return ResponseEntity.status(500).body(body);
  }
}
