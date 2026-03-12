package com.sprint.mission.discodeit.exception;

import com.sprint.mission.discodeit.exception.global.DiscodeitException;
import com.sprint.mission.discodeit.exception.global.ErrorCode;
import com.sprint.mission.discodeit.response.ErrorResponse;
import java.util.NoSuchElementException;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 모든 예외를 일관된 ErrorResponse 형식으로 응답하는 전역 예외 핸들러.
 *
 * @RestControllerAdvice로 컨트롤러 계층에서 발생한 예외를 처리하며, 모든 핸들러는 ResponseEntity&lt;ErrorResponse&gt;를
 * 반환한다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(DiscodeitException.class)
  public ResponseEntity<ErrorResponse> handleDiscodeitException(DiscodeitException e) {
    int status = resolveStatus(e.getErrorCode());
    ErrorResponse body = toErrorResponse(
        e.getTimestamp(),
        e.getErrorCode().name(),
        e.getMessage(),
        e.getDetails(),
        e.getClass().getName(),
        status
    );
    return ResponseEntity.status(status).body(body);
  }

  /**
   * 유효성 검증 실패 시 필드별 오류 메시지를 담은 ErrorResponse 반환.
   * details에는 필드명 -> 검증 실패 메시지가 포함된다.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
      MethodArgumentNotValidException e) {
    Map<String, Object> details = e.getBindingResult().getFieldErrors().stream()
        .collect(Collectors.toMap(
            err -> err.getField(),
            err -> err.getDefaultMessage() != null ? err.getDefaultMessage() : "",
            (a, b) -> a
        ));
    int errorCount = details.size();
    String message = errorCount == 0
        ? "요청 검증에 실패했습니다."
        : "요청 검증에 실패했습니다. (" + errorCount + "개 필드: " + String.join(", ", details.keySet()) + ")";
    ErrorResponse body = toErrorResponse(
        Instant.now(),
        "VALIDATION_ERROR",
        message,
        details,
        e.getClass().getName(),
        HttpStatus.BAD_REQUEST.value()
    );
    return ResponseEntity.badRequest().body(body);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
      HttpMessageNotReadableException e) {
    log.warn("요청 본문 파싱 실패: {}", e.getMessage());
    ErrorResponse body = toErrorResponse(
        Instant.now(),
        "INVALID_REQUEST_BODY",
        "요청 본문이 올바르지 않습니다.",
        Collections.emptyMap(),
        e.getClass().getName(),
        HttpStatus.BAD_REQUEST.value()
    );
    return ResponseEntity.badRequest().body(body);
  }

  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<ErrorResponse> handleNoSuchElement(NoSuchElementException e) {
    ErrorResponse body = toErrorResponse(
        Instant.now(),
        "NOT_FOUND",
        e.getMessage() != null ? e.getMessage() : "리소스를 찾을 수 없습니다.",
        Collections.emptyMap(),
        e.getClass().getName(),
        HttpStatus.NOT_FOUND.value()
    );
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
      IllegalArgumentException e) {
    ErrorResponse body = toErrorResponse(
        Instant.now(),
        "BAD_REQUEST",
        e.getMessage() != null ? e.getMessage() : "잘못된 요청입니다.",
        Collections.emptyMap(),
        e.getClass().getName(),
        HttpStatus.BAD_REQUEST.value()
    );
    return ResponseEntity.badRequest().body(body);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e) {
    log.error("Unhandled exception", e);
    ErrorResponse body = toErrorResponse(
        Instant.now(),
        "INTERNAL_ERROR",
        e.getMessage() != null ? e.getMessage() : "알 수 없는 오류가 발생했습니다.",
        Collections.emptyMap(),
        e.getClass().getName(),
        HttpStatus.INTERNAL_SERVER_ERROR.value()
    );
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
  }

  private static ErrorResponse toErrorResponse(
      Instant timestamp,
      String code,
      String message,
      Map<String, Object> details,
      String exceptionType,
      int status) {
    return new ErrorResponse(
        timestamp,
        code,
        message,
        details != null ? details : Collections.emptyMap(),
        exceptionType,
        status
    );
  }

  private static int resolveStatus(ErrorCode code) {
    return switch (code) {
      case USER_NOT_FOUND, CHANNEL_NOT_FOUND, MESSAGE_NOT_FOUND, BINARY_CONTENT_NOT_FOUND,
           READ_STATUS_NOT_FOUND, USER_STATUS_NOT_FOUND -> HttpStatus.NOT_FOUND.value();
      case DUPLICATE_USER, USER_STATUS_ALREADY_EXISTS, READ_STATUS_ALREADY_EXISTS -> HttpStatus.CONFLICT.value();
      case PRIVATE_CHANNEL_UPDATE, REQUEST_REQUIRED -> HttpStatus.BAD_REQUEST.value();
      case AUTH_FAILED -> HttpStatus.UNAUTHORIZED.value();
    };
  }
}
