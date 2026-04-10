package com.sprint.mission.discodeit.exception;

import com.sprint.mission.discodeit.dto.response.ErrorResponse;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  // Bean Validation 실패 시 발생하는 예외 처리
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException ex) {
    log.warn("입력값 검증 실패 - @RequestBody 검증 오류: {}", ex.getMessage());

    HttpStatus status = HttpStatus.BAD_REQUEST;

    Map<String, Object> fieldErrors = new HashMap<>();

    // 필드별 오류 메시지 수집
    ex.getBindingResult().getAllErrors().forEach(error -> {
      if (error instanceof FieldError) {
        FieldError fieldError = (FieldError) error;
        fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
      } else {
        fieldErrors.put("global", error.getDefaultMessage());
      }
    });

    ErrorResponse errorResponse = new ErrorResponse(
        Instant.now(),
        "VALIDATION_FAILED",
        "입력 데이터 검증에 실패했습니다",
        fieldErrors,
        ex.getClass().getSimpleName(),
        status.value()
    );

    return ResponseEntity.status(status).body(errorResponse);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
    log.warn("권한 부족 액세스 거부: {}", ex.getMessage());

    ErrorCode errorCode = ErrorCode.ACCESS_DENIED;
    HttpStatus status = errorCode.getStatus();

    ErrorResponse errorResponse = new ErrorResponse(
        Instant.now(),
        errorCode.name(),
        errorCode.getMessage(),
        null,
        ex.getClass().getSimpleName(),
        status.value()
    );

    return ResponseEntity.status(status).body(errorResponse);
  }

  // 커스텀 예외 처리
  @ExceptionHandler(DiscodeitException.class)
  public ResponseEntity<ErrorResponse> handleDiscodeitException(DiscodeitException ex) {
    log.error("예외 발생 - code: {}, message: {}", ex.getErrorCode(),
        ex.getMessage());

    HttpStatus status = ex.getErrorCode().getStatus();

    ErrorResponse errorResponse = new ErrorResponse(
        ex.getTimestamp(),
        ex.getErrorCode().name(),
        ex.getMessage(),
        ex.getDetails(),
        ex.getClass().getSimpleName(),
        status.value()
    );

    return ResponseEntity.status(status).body(errorResponse);
  }

  // Fallback 핸들러
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception ex) {
    log.error("예상치 못 한 예외 발생", ex);

    ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
    HttpStatus status = errorCode.getStatus();

    ErrorResponse errorResponse = new ErrorResponse(
        Instant.now(),
        errorCode.name(),
        errorCode.getMessage(),
        null,
        ex.getClass().getSimpleName(),
        status.value()
    );

    return ResponseEntity.status(status).body(errorResponse);
  }
}
