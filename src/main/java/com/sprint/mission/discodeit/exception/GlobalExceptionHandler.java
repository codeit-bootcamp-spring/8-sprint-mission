package com.sprint.mission.discodeit.exception;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(DiscodeitException.class)
  public ResponseEntity<ErrorResponse> handleDiscodeitException(DiscodeitException e) {
    ErrorCode errorCode = e.getErrorCode();
    log.warn("Discode.it Exception: {} - {}", errorCode.getCode(), errorCode.getMessage());
    if (!e.getDetails().isEmpty()) {
      log.warn("Details: {}", e.getDetails());
    }
    return ResponseEntity
        .status(errorCode.getStatus())
        .body(ErrorResponse.of(e));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationExceptions(
      MethodArgumentNotValidException e) {
    Map<String, Object> details = new HashMap<>();
    e.getBindingResult().getAllErrors().forEach((error) -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      details.put(fieldName, errorMessage);
    });
    log.warn("Validation failed: {}", details);

    DiscodeitException discodeitException = new DiscodeitException(ErrorCode.INVALID_REQUEST,
        details);
    return ResponseEntity
        .status(discodeitException.getErrorCode().getStatus())
        .body(ErrorResponse.of(discodeitException));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception e) {
    log.error("Unexpected Exception", e);
    DiscodeitException discodeitException = new DiscodeitException(ErrorCode.INTERNAL_SERVER_ERROR);
    return ResponseEntity
        .status(discodeitException.getErrorCode().getStatus())
        .body(ErrorResponse.of(discodeitException));
  }
}
