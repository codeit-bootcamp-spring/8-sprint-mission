package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.DTO.dto.UserDto;
import org.springframework.security.core.userdetails.UserDetails;

public interface AuthService {
    UserDto getCurrentUserInfo(UserDetails userDetails);
}
