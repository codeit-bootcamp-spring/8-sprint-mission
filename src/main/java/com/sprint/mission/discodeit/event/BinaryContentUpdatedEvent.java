package com.sprint.mission.discodeit.event;

import java.util.UUID;

public record BinaryContentUpdatedEvent(
    UUID binaryContentId,
    UUID uploaderId
) {

}
