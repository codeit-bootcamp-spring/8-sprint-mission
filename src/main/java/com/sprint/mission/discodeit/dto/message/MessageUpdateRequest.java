package com.sprint.mission.discodeit.dto.message;


import java.util.UUID;

/*
    MessageUpdateRequest
    -------------------------
    메시지 수정용 DTO

    [필드 설명]
    • id                : 수정할 대상 메시지 id
    • contents          : 메시지 내용
 */
public record MessageUpdateRequest(
        UUID id,
        String contents
) {
}
