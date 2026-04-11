package com.sprint.mission.discodeit.dto.data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MessageDto(
		UUID id,
		String content,
		UUID channelId,
		UUID channel,
		UUID authorId,
		UserDto author,
		List<UUID> attachmentIds,
		List<BinaryContentDto> attachments,
		Instant createdAt
) {

}
