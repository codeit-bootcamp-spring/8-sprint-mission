package com.sprint.mission.discodeit.dto.readstatus;


import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

/*
    ReadStatusCreateRequest
    -------------------------
    ReadStatus 생성용 DTO

    [필드 설명]
    • userId            : 유저 id
    • channelId         : 채널 id
    • lastReadAt        : 마지막 읽은 시각
 */
public record ReadStatusCreateRequest(

    @NotNull
    UUID userId,

    @NotNull
    UUID channelId,

    @NotNull
    Instant lastReadAt
) {

}
