package com.sprint.mission.discodeit.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/*
    UserCreateRequest
    -------------------------
    유저 추가 시 넘겨주는 데이터를 모아놓은 DTO (record)

    [필드 설명]
    • username                  : 유저 이름
    • email                     : 유저 이메일 주소
    • password                  : 유저 패스워드 (저장시에만 받아오고, 응답에는 넘겨주지 않는다.)
 */
public record UserCreateRequest(
    @NotBlank
    @Size(max = 50)
    String username,

    @NotBlank
    @Email
    @Size(max = 100)
    String email,

    @NotBlank
    @Size(min = 1, max = 60)
    String password
) {

}
