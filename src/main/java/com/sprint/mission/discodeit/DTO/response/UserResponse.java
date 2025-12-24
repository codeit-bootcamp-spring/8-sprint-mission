package com.sprint.mission.discodeit.DTO.response;

import com.sprint.mission.discodeit.entity.UserStatusType;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String userId,
        String name,
        String email,
        char gender,
        int grade,
        Optional<UUID> profileId,
        UserStatusType userStatusType,
        Instant createdAt,
        Instant modifiedAt

) {
    public static UserResponse from(User user, UserStatus userStatus) {
        return new UserResponse(
                user.getId(),
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getGender(),
                user.getGrade(),
                Optional.ofNullable(user.getProfileId()),
                userStatus.getCurrentStatus(),
                user.getCreatedAt(),
                user.getModifiedAt()
        );
    }
}
