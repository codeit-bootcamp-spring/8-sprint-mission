package com.sprint.mission.discodeit.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/*
    컨트롤러 전역 예외 처리
    모든 예외는 이곳에서 처리한 뒤
    일관 된 JSON 에러 응답을 내려준다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  /*
    "리소스를 찾을 수 없음" 계열
     예) userRepository.findById(...).orElseThrow(NoSuchElementException)
   */
  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<ErrorResponse> handleNoSuchElement(
      NoSuchElementException e,
      HttpServletRequest request
  ) {
    return build(HttpStatus.NOT_FOUND, e, request);
  }

  /*
    "요청이 잘못됨" 계열
     예) 비밀번호 불일치, 중복 newUsername, 파라미터 검증 실패를 IllegalArgumentException으로 던지는 경우
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(
      IllegalArgumentException e,
      HttpServletRequest request
  ) {
    return build(HttpStatus.BAD_REQUEST, e, request);
  }

  /*
    파라미터 타입이 안 맞을 때 (UUID 자리에 이상한 문자열이 들어오는 등)
   */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch(
      MethodArgumentTypeMismatchException e,
      HttpServletRequest request
  ) {
    return build(HttpStatus.BAD_REQUEST, e, request);
  }

  /*
    JSON 파싱 실패(바디가 깨졌거나 형식이 잘못된 경우)
     주로 @ModelAttribute라서 빈도는 낮을 수 있지만, API 확장 시 유용
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleNotReadable(
      HttpMessageNotReadableException e,
      HttpServletRequest request
  ) {
    return build(HttpStatus.BAD_REQUEST, e, request);
  }

  /*
    파일 업로드 용량 초과(멀티파트)
   */
  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ErrorResponse> handleMaxUploadSize(
      MaxUploadSizeExceededException e,
      HttpServletRequest request
  ) {
    HttpStatus status = HttpStatus.PAYLOAD_TOO_LARGE;
    ErrorResponse body = new ErrorResponse(
        Instant.now(),
        status.value(),
        status.getReasonPhrase(),
        "업로드 파일 용량이 제한을 초과했습니다.",
        request.getRequestURI()
    );
    return ResponseEntity.status(status).body(body);
  }

  /*
    위에서 못 잡은 나머지 모든 예외 (최후의 안전망)
     내부 에러는 500으로
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(
      Exception e,
      HttpServletRequest request
  ) {
    return build(HttpStatus.INTERNAL_SERVER_ERROR, e, request);
  }


  private ResponseEntity<ErrorResponse> build(
      HttpStatus status,
      Exception e,
      HttpServletRequest request
  ) {
    ErrorResponse body = new ErrorResponse(
        Instant.now(),
        status.value(),
        status.getReasonPhrase(),
        e.getMessage(),
        request.getRequestURI()
    );
    return ResponseEntity.status(status).body(body);
  }
}
