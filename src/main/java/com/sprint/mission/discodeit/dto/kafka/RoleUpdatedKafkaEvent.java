package com.sprint.mission.discodeit.dto.kafka;

import com.sprint.mission.discodeit.entity.Role;
import java.util.UUID;

public record RoleUpdatedKafkaEvent(
    UUID userId,
    String username,
    Role oldRole,
    Role newRole
) {
}
