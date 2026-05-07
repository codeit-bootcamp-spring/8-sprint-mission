package com.sprint.mission.discodeit.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode
{
    USER_NOT_FOUND("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    BINARY_CONTENT_NOT_FOUND("바이너리 컨텐츠를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CHANNEL_NOT_FOUND("채널을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    MESSAGE_NOT_FOUND("메시지를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    READSTATUS_NOT_FOUND("읽음 상태를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    USERSTATUS_NOT_FOUND("사용자 상태를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    DUPLICATE_USER("이미 존재하는 사용자입니다.", HttpStatus.CONFLICT),
    DUPLICATE_CHANNEL("이미 존재하는 채널입니다.", HttpStatus.CONFLICT),
    DUPLICATE_READSTATUS("이미 해당 채널의 읽음 상태가 존재합니다.", HttpStatus.CONFLICT),

    PRIVATE_CHANNEL_UPDATE("비공개 채널은 수정할 수 없습니다.", HttpStatus.FORBIDDEN),

    PASSWORD_MISMATCH("비밀번호 또는 아이디가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),

    SESSION_INVALIDATION_FAILED("세션 무효화에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    INVALID_TOKEN("유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
    MISSING_TOKEN_CLAIM("토큰에 필요한 정보가 없습니다.", HttpStatus.UNAUTHORIZED),
    TOKEN_GENERATION_FAILED("토큰 생성에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    NOTIFICATION_NOT_FOUND("알림을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    NOTIFICATION_FORBIDDEN("본인의 알림만 삭제할 수 있습니다.", HttpStatus.FORBIDDEN),

    USER_MISMATCH("인증된 사용자 정보가 일치하지 않습니다.", HttpStatus.FORBIDDEN),
    LOCK_ACQUISITION_FAILED("락 획득 실패", HttpStatus.SERVICE_UNAVAILABLE);

    private final int status;
    private final String message;

    ErrorCode(String message, HttpStatus status) {
        this.message = message;
        this.status = status.value();
    }
}
