package com.sprint.mission.discodeit.event;

import java.util.List;
import java.util.UUID;

public record SseBroadcastMessage(
    String eventName,
    Object data,
    List<UUID> targetUserIds
) {

}
