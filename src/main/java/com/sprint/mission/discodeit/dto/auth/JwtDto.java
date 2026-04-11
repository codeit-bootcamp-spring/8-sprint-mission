package com.sprint.mission.discodeit.dto.auth;

import com.sprint.mission.discodeit.dto.user.UserDto;

/**
 * JWT 인증 응답 DTO - accessToken: API 요청 시 Authorization 헤더에 포함될 토큰 - userDto: 로그인한 사용자 정보
 */
public record JwtDto(
    String accessToken,
    UserDto userDto
) {
}
