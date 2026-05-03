package com.sprint.mission.discodeit.dto.data;

import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import java.util.UUID;

public record BinaryContentWithBytesDto(
		UUID id,
		String fileName,
		Long size,
		String contentType,
		BinaryContentStatus status,
		String bytes
) {

}
