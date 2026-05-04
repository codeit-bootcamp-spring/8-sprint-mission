package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.dto.UserDto;
import com.sprint.mission.discodeit.dto.request.RoleUpdateRequest;
import com.sprint.mission.discodeit.security.jwt.store.JwtInformation;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    UserDto updateRole(RoleUpdateRequest request);

    JwtInformation refreshToken(String refreshToken, HttpServletResponse response);
}
