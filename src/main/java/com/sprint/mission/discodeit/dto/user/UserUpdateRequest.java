package com.sprint.mission.discodeit.dto.user;

import java.util.UUID;

/*
    UserUpdateRequest
    -------------------------
    유저 수정 시 넘겨주는 데이터를 모아놓은 DTO (record)

    [필드 설명]
    • userId                           : 유저 userId (수정 대상)
    • newUsername                  : 유저 이름
    • newEmail                     : 유저 이메일 주소
    • newPassword                  : 유저 패스워드
    • profileImageFilename         : 프로필 이미지 파일 명 (이미지(파일)는 id를 가져와서 수정하는게 아닌 새로 생성)
    • profileImageData             : 프로필 이미지 파일 데이터
 */
public record UserUpdateRequest(
    UUID id,
    String newUsername,
    String newEmail,
    String newPassword,
    String profileImageFilename,
    byte[] profileImageData
) {

}
