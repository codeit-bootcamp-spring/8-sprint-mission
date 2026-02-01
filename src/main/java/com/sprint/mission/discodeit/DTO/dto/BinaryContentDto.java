package com.sprint.mission.discodeit.DTO.dto;

import java.util.UUID;

public record BinaryContentDto(
    UUID id,
    String fileName,
    Long size,
    String contentType
) {

}
