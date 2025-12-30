package com.sprint.mission.discodeit.dto.readstatus;


import java.time.Instant;
import java.util.UUID;

/*
    ReadStatusUpdateRequest
    -------------------------
    ReadStatus 수정용 DTO

    [필드 설명]
    • userId              : 수정 될 ReadStatus의 userId
    • lastActiveAt        : 변경할 읽을 시각
 */
public record ReadStatusUpdateRequest(
    Instant lastActiveAt
) {

}
