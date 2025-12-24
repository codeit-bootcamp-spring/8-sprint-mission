package com.sprint.mission.discodeit.dto.user;

import java.util.UUID;

/*
    UserUpdateRequest
    -------------------------
    유저 수정 시 넘겨주는 데이터를 모아놓은 DTO (record)

    [필드 설명]
    • id                        : 유저 id (수정 대상)
    • username                  : 유저 이름
    • email                     : 유저 이메일 주소
    • password                  : 유저 패스워드
    • profileImageFilename      : 프로필 이미지 파일 명 (이미지(파일)는 id를 가져와서 수정하는게 아닌 새로 생성)
    • profileImageData          : 프로필 이미지 파일 데이터
 */
public record UserUpdateRequest(
        UUID id,
        String username,
        String email,
        String password,
        String profileImageFilename,
        byte[] profileImageData
) {
}
