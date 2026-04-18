package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.dto.UserDto;
import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.service.auth.DiscodeitUserDetails;

public interface AuthService {
    UserDto updateRoleInternal(UserRoleUpdateRequest userRoleUpdateRequest);
}
