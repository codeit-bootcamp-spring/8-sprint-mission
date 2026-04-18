package com.sprint.mission.discodeit.auth.dto;

import com.sprint.mission.discodeit.dto.data.UserDto;

/**
 * JWT 응답 DTO
 * 로그인/재발급 시 사용자 정보와 액세스 토큰을 함께 반환한다.
 * 프론트엔드는 { userDto, accessToken } 구조를 기대한다.
 */
public record JwtDTO(
    UserDto userDto,
    String accessToken
) {
}
