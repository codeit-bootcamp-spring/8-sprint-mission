package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.LoginRequest;
import com.sprint.mission.discodeit.dto.UserResponse;

public interface AuthService {
    UserResponse login(String email, String password);

    UserResponse login(LoginRequest request);
}