package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.DTO.request.LoginRequest;
import com.sprint.mission.discodeit.DTO.response.UserResponse;

import java.util.UUID;

public interface AuthService {
    UserResponse login(LoginRequest request);
    String logout(UUID userId);
}
