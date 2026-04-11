package com.sprint.mission.discodeit.DTO.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * JWT 응답 DTO
 * 로그인/재발급 시 사용자의 정보와 엑세스 토큰을 함께 반환
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class JwtDTO {

    private UserDto userDto;
    private String accessToken;
}
