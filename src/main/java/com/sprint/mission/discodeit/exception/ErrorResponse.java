package com.sprint.mission.discodeit.exception;

import java.time.Instant;
import java.util.Map;

/**
 * @param timestamp     발생 시각
 * @param code          코드
 * @param message       메시지 내용
 * @param details       맥락값
 * @param exceptionType 발생한 예외의 클래스 이름
 * @param status        HTTP 상태코드
 */
public record ErrorResponse(
    Instant timestamp,
    String code,
    String message,
    Map<String, Object> details,
    String exceptionType,
    int status
) {

}
