package com.sprint.mission.discodeit.dto.user;


/*
    AuthLoginRequest
    -------------------------
    로그인 요청 시 사용 될 DTO

    [필드 설명]
    • username          : 유저 이름
    • password          : 유저 패스워드
 */
public record AuthLoginRequest(
        String username,
        String password
) {
}
