package com.sprint.mission.discodeit.exception;

import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(DiscodeitException.class)
  public ResponseEntity<ErrorResponse> handleDiscodeitException(DiscodeitException e) {
    ErrorResponse response = ErrorResponse.builder()
        .timestamp(e.getTimestamp())
        .code(e.getErrorCode().name()) // Enum의 이름 (예: USER_NOT_FOUND)
        .message(e.getMessage())
        .details(e.getDetails())
        .exceptionType(e.getClass().getSimpleName()) // 클래스명 (예: UserException)
        .status(400) // 나중에 ErrorCode에 status를 추가하면 e.getErrorCode().getStatus()로 연동 가능
        .build();

    return ResponseEntity.status(response.getStatus()).body(response);
  }

  // 2. 유효성 검사(Validation) 실패 시 발생하는 예외 처리 (미션 요구사항)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException e) {
    ErrorResponse response = ErrorResponse.builder()
        .timestamp(Instant.now())
        .code("INVALID_INPUT_VALUE")
        .message("입력값이 유효하지 않습니다.")
        .exceptionType(e.getClass().getSimpleName())
        .status(HttpStatus.BAD_REQUEST.value())
        .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  // 3. 그 외 예상치 못한 모든 에러 처리
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e) {
    e.printStackTrace(); // 나중에 log.error()로 바꾸면 더 좋아요!

    ErrorResponse response = ErrorResponse.builder()
        .timestamp(Instant.now())
        .code("INTERNAL_SERVER_ERROR")
        .message("서버 내부에서 알 수 없는 오류가 발생했습니다.")
        .exceptionType(e.getClass().getSimpleName())
        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
        .build();

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }
}
