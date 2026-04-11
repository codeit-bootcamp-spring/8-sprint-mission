package com.sprint.mission.discodeit.dto.user;

import com.sprint.mission.discodeit.entity.UserRole;
import java.util.UUID;

public record UserRoleUpdateRequest(
    UUID userId,
    UserRole newRole
) {

}
