package com.sprint.mission.discodeit.dto.user;


/*
    AuthLoginRequest
    -------------------------
    로그인 요청 시 사용 될 DTO

    [필드 설명]
    • newUsername          : 유저 이름
    • newPassword          : 유저 패스워드
 */
public record LoginRequest(
    String username,
    String password
) {

}
