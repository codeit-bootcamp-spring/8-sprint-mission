package com.sprint.mission.discodeit.security.jwt.store;

import com.sprint.mission.discodeit.dto.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtInformation {
    private UserDto userDto;
    private String accessToken;
    private String refreshToken;

    // AT/RT를 새로운 토큰으로 교체
    public void rotate(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
