package com.sprint.mission.discodeit.DTO.request;

import com.sprint.mission.discodeit.entity.UserStatusType;

public record UserStatusUpdateRequest(
        UserStatusType userStatusType
) {
}
