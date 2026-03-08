package com.sprint.mission.discodeit.dto.data;

import java.util.UUID;

public record BinaryContentWithBytesDto(
		UUID id,
		String fileName,
		Long size,
		String contentType,
		String bytes
) {

}
