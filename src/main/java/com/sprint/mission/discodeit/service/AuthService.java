package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.DTO.request.LoginRequest;
import com.sprint.mission.discodeit.DTO.response.UserResponse;

public interface AuthService {
    UserResponse login(LoginRequest request);
}
