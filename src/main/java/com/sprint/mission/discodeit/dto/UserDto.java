package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.Role;
import java.util.UUID;

public record UserDto(
    UUID id,
    String username, // 사용자의 이름
    String email,
    BinaryContentDto profile,
    Role role
) {

}
