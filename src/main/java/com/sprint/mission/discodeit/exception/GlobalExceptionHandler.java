package com.sprint.mission.discodeit.exception;

import com.sprint.mission.discodeit.exception.global.DiscodeitException;
import com.sprint.mission.discodeit.exception.global.ErrorCode;
import com.sprint.mission.discodeit.response.ErrorResponse;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(DiscodeitException.class)
  public ResponseEntity<ErrorResponse> handleDiscodeitException(DiscodeitException e) {
    int status = resolveStatus(e.getErrorCode());
    ErrorResponse body = new ErrorResponse(
        e.getTimestamp(),
        e.getErrorCode().name(),
        e.getMessage(),
        e.getDetails(),
        e.getClass().getName(),
        status
    );
    return ResponseEntity.status(status).body(body);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e) {
    log.error("Unhandled exception", e);
    ErrorResponse body = new ErrorResponse(
        java.time.Instant.now(),
        "INTERNAL_ERROR",
        e.getMessage() != null ? e.getMessage() : "알 수 없는 오류가 발생했습니다.",
        Collections.emptyMap(),
        e.getClass().getName(),
        HttpStatus.INTERNAL_SERVER_ERROR.value()
    );
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
  }

  private static int resolveStatus(ErrorCode code) {
    return switch (code) {
      case USER_NOT_FOUND, CHANNEL_NOT_FOUND, MESSAGE_NOT_FOUND, BINARY_CONTENT_NOT_FOUND,
          READ_STATUS_NOT_FOUND, USER_STATUS_NOT_FOUND -> HttpStatus.NOT_FOUND.value();
      case DUPLICATE_USER, USER_STATUS_ALREADY_EXISTS -> HttpStatus.CONFLICT.value();
      case PRIVATE_CHANNEL_UPDATE, AUTH_FAILED, REQUEST_REQUIRED -> HttpStatus.BAD_REQUEST.value();
    };
  }
}
