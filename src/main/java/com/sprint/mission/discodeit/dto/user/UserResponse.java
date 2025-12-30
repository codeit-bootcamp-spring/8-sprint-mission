package com.sprint.mission.discodeit.dto.user;

import java.time.Instant;
import java.util.UUID;

/*
    UserDto
    -------------------------
    클라이언트에 응답 데이터를 넘겨주는 DTO (record)

    [필드 설명]
    • userId                  : 유저 userId
    • createdAt           : 생성 시간
    • updatedAt           : 수정 시간
    • newUsername         : 유저 이름
    • newEmail            : 유저 이메일 주소
    • online              : 유저 온라인 여부 (5분 지나지 않아야 온라인)
    • newLastActiveAt        : 유저의 마지막 접속 시간
    • profileImageId      : 프로필 이미지 userId
 */
public record UserResponse(
    UUID id,
    Instant createdAt,
    Instant updatedAt,
    String username,
    String email,
    boolean online,
    Instant lastActiveAt,
    UUID profileImageId
) {

}
