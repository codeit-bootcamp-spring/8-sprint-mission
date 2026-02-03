package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final BinaryContentMapper binaryContentMapper;

    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        // 프로필 이미지 변환
        BinaryContentDto profileDto = null;
        if (user.getProfile() != null) {
            profileDto = binaryContentMapper.toDto(user.getProfile());
        }

        // 온라인 상태 확인 (UserStatus에서)
        boolean online = false;
        if (user.getStatus() != null) {
            online = user.getStatus().isOnline();
        }

        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .profile(profileDto)
                .online(online)
                .build();
    }
}
