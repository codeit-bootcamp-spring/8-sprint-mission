package com.sprint.mission.discodeit.dto.user;

/*
    UserCreateRequest
    -------------------------
    유저 추가 시 넘겨주는 데이터를 모아놓은 DTO (record)

    [필드 설명]
    • newUsername                  : 유저 이름
    • newEmail                     : 유저 이메일 주소
    • newPassword                  : 유저 패스워드 (저장시에만 받아오고, 응답에는 넘겨주지 않는다.)
    • profileImageFilename      : 프로필 이미지 파일 명
    • profileImageData          : 프로필 이미지 파일 데이터
 */
public record UserCreateRequest(
    String username,
    String email,
    String password
) {

}
