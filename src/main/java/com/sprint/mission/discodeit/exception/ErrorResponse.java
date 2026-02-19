package com.sprint.mission.discodeit.exception;

import java.time.Instant;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {

  private final Instant timestamp; //예외 발생 시간
  private final String code; //에러코드의 이름
  private final String message; //에러 메세지
  private final Map<String, Object> details; //구체적인 추가 정보
  private final String exceptionType; //발생한 예외의 클래스 이름
  private final int status; //HTTP 상태코드

}
