package com.sprint.mission.discodeit.event;

import java.util.Collection;
import java.util.UUID;

public record SseKafkaEvent(
    UUID eventId,
    Collection<UUID> receiverIds,
    String eventName,
    Object data
) {

}
