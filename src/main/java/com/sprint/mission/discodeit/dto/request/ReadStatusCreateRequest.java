package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record ReadStatusCreateRequest(
    @NotNull(message = "채널 ID는 필수입니다")
    UUID channelId,

    @NotNull(message = "마지막 읽은 시간은 필수입니다")
    //@PastOrPresent(message = "마지막 읽은 시간은 현재 또는 과거 시간이어야 합니다")
    Instant lastReadAt
) {

}
