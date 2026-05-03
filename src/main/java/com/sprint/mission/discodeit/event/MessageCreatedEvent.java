package com.sprint.mission.discodeit.event;

import java.util.UUID;

public record MessageCreatedEvent(
    UUID messageId,
    String content,
    UUID channelId,
    UUID authorId,
    String username,
    String channelName
) {

}
