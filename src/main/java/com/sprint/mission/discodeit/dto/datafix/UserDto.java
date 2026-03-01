package com.sprint.mission.discodeit.dto.datafix;

import java.util.UUID;

public record UserDto(
		UUID id,
		String username,
		String email,
		UUID profileId,
		Boolean online
) {

}
