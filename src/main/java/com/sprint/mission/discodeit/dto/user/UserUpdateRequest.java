package com.sprint.mission.discodeit.dto.user;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/*
    UserUpdateRequest
    -------------------------
    유저 수정 시 넘겨주는 데이터를 모아놓은 DTO (record)

    [필드 설명]
    • newUsername                  : 유저 이름
    • newEmail                     : 유저 이메일 주소
    • newPassword                  : 유저 패스워드
    • profile                      : 프로필 이미지 파일 데이터
 */
public record UserUpdateRequest(

    @Size(max = 50)
    String newUsername,

    @Email
    @Size(max = 100)
    String newEmail,

    @Size(min = 1, max = 60)
    String newPassword,

    BinaryContentCreateRequest profile
) {

}
