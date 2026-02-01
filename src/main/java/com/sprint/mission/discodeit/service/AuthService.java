package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.DTO.dto.UserDto;
import com.sprint.mission.discodeit.DTO.request.LoginRequest;

public interface AuthService {

  UserDto login(LoginRequest request);
}
