package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.DTO.dto.UserDto;
import com.sprint.mission.discodeit.service.auth.DiscodeitUserDetails;

public interface AuthService {
    UserDto getCurrentUserInfo(DiscodeitUserDetails userDetails);
}
