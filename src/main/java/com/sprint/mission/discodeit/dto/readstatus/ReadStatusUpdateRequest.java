package com.sprint.mission.discodeit.dto.readstatus;


import java.time.Instant;
import java.util.UUID;

/*
    ReadStatusUpdateRequest
    -------------------------
    ReadStatus 수정용 DTO

    [필드 설명]
    • newLastReadAt        : 변경할 읽을 시각
 */
public record ReadStatusUpdateRequest(
    Instant newLastReadAt
) {

}
