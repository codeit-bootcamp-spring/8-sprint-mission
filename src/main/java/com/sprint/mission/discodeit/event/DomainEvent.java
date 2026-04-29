package com.sprint.mission.discodeit.event;

import java.util.List;
import java.util.UUID;

public record DomainEvent<T>(
    String eventName,
    T data,
    List<UUID> targetUserIds
) {

}
