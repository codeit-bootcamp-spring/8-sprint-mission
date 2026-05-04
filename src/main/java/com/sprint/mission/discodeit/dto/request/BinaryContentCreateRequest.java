package com.sprint.mission.discodeit.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "생성할 BinaryContent 정보")
public record BinaryContentCreateRequest(
        String fileName,
        String contentType,
        byte[] bytes
) {

}
