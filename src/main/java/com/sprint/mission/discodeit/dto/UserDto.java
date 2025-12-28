package com.sprint.mission.discodeit.dto;

import java.util.UUID;

/**
 * [심화 요구사항] 사용자 정보 전송 객체
 * 필드명은 화면(script.js) 바인딩 규격에 맞춥니다.
 */
public record UserDto(
        UUID id,
        String username, //  'name' 대신 'username'을 요구하는 경우가 많습니다.
        String email,
        UUID profileId,  //  Postman에서 확인된 UUID 값
        boolean online   //  online 여부
) {}