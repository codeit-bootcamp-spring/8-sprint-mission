package com.sprint.mission.discodeit.event.Sse.BinaryContent;

import com.sprint.mission.discodeit.dto.dto.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;

public record BinaryContentUpdatedEvent(
        BinaryContentDto binaryContentDto,
        BinaryContentStatus status
) {
}
