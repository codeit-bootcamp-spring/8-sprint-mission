package com.sprint.mission.discodeit.dto.user;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/*
    AuthLoginRequest
    -------------------------
    로그인 요청 시 사용 될 DTO

    [필드 설명]
    • username          : 유저 이름
    • password          : 유저 패스워드
 */
public record LoginRequest(
    @NotBlank
    @Size(max = 50)
    String username,

    @NotBlank
    @Size(min = 1, max = 60)
    String password
) {

}
