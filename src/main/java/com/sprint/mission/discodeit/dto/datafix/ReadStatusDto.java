package com.sprint.mission.discodeit.dto.datafix;

import java.time.Instant;
import java.util.UUID;

public record ReadStatusDto(
		UUID id,
		UUID userId,
		UUID channelId,
		Instant lastReadAt
) {

}
