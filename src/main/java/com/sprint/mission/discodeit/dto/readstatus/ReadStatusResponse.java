package com.sprint.mission.discodeit.dto.readstatus;


import java.time.Instant;
import java.util.UUID;

/*
    ReadStatusDto
    -------------------------
    ReadStatus 조회용 DTO

    [필드 설명]
    • userId              : ReadStatus userId
    • authorId            : 유저 userId
    • channelId           : 채널 userId
    • lastActiveAt        : 마지막 읽은 시각
 */
public record ReadStatusResponse(
    UUID id,
    UUID userId,
    UUID channelId,
    Instant lastActiveAt
) {

}
