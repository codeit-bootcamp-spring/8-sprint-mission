package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.entity.DiscodeitUserDetails;

public interface AuthService {

  UserDto updateUserRole(UserRoleUpdateRequest userRoleUpdateRequest);

  UserDto getCurrentUserInfo(DiscodeitUserDetails userDetails);
}
