package com.sprint.mission.discodeit.dto.userstatus;

import java.time.Instant;
import java.util.UUID;

/*
    UserStatusDto
    -------------------------
    UserStatus 조회용 DTO

    [필드 설명]
    • id                  : 유저 접속 정보 id
    • userId              : 유저 id
    • lastConnAt          : 유저의 마지막 접속 시간
    • online              : 유저 온라인 여부 (5분 지나지 않아야 온라인)
 */
public record UserStatusDto(
        UUID id,
        UUID userId,
        Instant lastConnAt,
        boolean online
) {
}
