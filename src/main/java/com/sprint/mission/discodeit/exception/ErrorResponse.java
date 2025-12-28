package com.sprint.mission.discodeit.exception;

import java.time.Instant;

/*
    API 에러 응답을 한 형태로 통일하기 위한 DTO
    timestamp:      언제 발생했는지
    status:         HTTP 상태코드 (400, 404, 500...)
    error:          상태 이름 (Bad Request, Not Found...)
    message:        예외 메시지
    path:           어떤 요청 URL에서 발생했는지
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
