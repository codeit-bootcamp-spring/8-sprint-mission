package com.sprint.mission.discodeit.event.Sse.BinaryContent;

import java.util.UUID;

public record BinaryContentDeletedEvent(
        UUID binaryContentId
) {
}
