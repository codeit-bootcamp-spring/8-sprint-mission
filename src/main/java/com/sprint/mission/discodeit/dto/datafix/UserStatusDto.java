package com.sprint.mission.discodeit.dto.datafix;

import java.time.Instant;
import java.util.UUID;

public record UserStatusDto(
		UUID id,
		UUID userId,
		Instant lastActiveAt
) {

}
