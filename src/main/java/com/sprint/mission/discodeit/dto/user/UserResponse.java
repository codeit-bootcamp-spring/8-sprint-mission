package com.sprint.mission.discodeit.dto.user;

import java.time.Instant;
import java.util.UUID;

/*
    UserDto
    -------------------------
    클라이언트에 응답 데이터를 넘겨주는 DTO (record)

    [필드 설명]
    • id                  : 유저 id
    • username            : 유저 이름
    • email               : 유저 이메일 주소
    • online              : 유저 온라인 여부 (5분 지나지 않아야 온라인)
    • lastConnAt          : 유저의 마지막 접속 시간
    • profileImageId      : 프로필 이미지 id
 */
public record UserResponse(
        UUID id,
        String username,
        String email,
        boolean online,
        Instant lastConnAt,
        UUID profileImageId
) {
}
