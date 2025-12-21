package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import java.util.UUID;

@Getter
public class ReadStatus {
    private UUID id;
    private UUID userId;
    private UUID channelId;
    private UUID lastReadMessageId;

    public ReadStatus() {}

    public ReadStatus(UUID userId, UUID channelId, UUID lastReadMessageId) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.channelId = channelId;
        this.lastReadMessageId = lastReadMessageId;
    }

    public void updateLastReadMessage(UUID lastReadMessageId) {
        this.lastReadMessageId = lastReadMessageId;
    }
}