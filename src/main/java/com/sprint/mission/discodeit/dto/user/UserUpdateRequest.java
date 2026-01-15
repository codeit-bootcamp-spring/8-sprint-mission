package com.sprint.mission.discodeit.dto.user;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import java.util.UUID;

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
    String newUsername,
    String newEmail,
    String newPassword,
    BinaryContentCreateRequest profile
) {

}
