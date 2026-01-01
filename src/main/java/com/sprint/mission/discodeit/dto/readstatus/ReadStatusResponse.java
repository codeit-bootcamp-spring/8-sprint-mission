package com.sprint.mission.discodeit.dto.readstatus;


import java.time.Instant;
import java.util.UUID;

/*
    ReadStatusDto
    -------------------------
    ReadStatus 조회용 DTO

    [필드 설명]
    • id                : ReadStatus id
    • userId            : 유저 id
    • channelId         : 채널 id
    • lastReadAt        : 마지막 읽은 시각
 */
public record ReadStatusResponse(
        UUID id,
        UUID userId,
        UUID channelId,
        Instant lastReadAt
) {
}
