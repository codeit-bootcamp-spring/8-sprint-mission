package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.UserStatusDto;
import com.sprint.mission.discodeit.entity.UserStatus;
import org.springframework.stereotype.Component;

@Component
public class UserStatusMapper {

    public UserStatusDto toDto(UserStatus userStatus) {
        if (userStatus == null) {
            return null;
        }

        return UserStatusDto.builder()
                .id(userStatus.getId())
                .userId(userStatus.getUserId())
                .lastActiveAt(userStatus.getLastActiveAt())
                .build();
    }
}
