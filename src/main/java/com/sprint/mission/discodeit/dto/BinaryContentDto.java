package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.base.BinaryContentStatus;
import java.util.UUID;

public record BinaryContentDto(
    UUID id,
    String fileName,
    Long size,
    String contentType,
    BinaryContentStatus status
) {

}
