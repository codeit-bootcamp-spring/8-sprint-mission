package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import java.time.Instant;
import java.util.UUID;

@Getter
public class ReadStatus {
    private UUID id;
    private UUID userId;
    private UUID channelId;
    private UUID lastReadMessageId;
    private Instant lastReadAt;

    public ReadStatus() {}

    public ReadStatus(UUID userId, UUID channelId, UUID lastReadMessageId) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.channelId = channelId;
        this.lastReadMessageId = lastReadMessageId;
    }

    public ReadStatus(UUID userId, UUID channelId, Instant lastReadAt) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.channelId = channelId;
        this.lastReadAt = lastReadAt;
    }

    public void updateLastReadMessage(UUID lastReadMessageId) {
        this.lastReadMessageId = lastReadMessageId;
    }
    
    public void updateLastReadAt(Instant lastReadAt) {
        this.lastReadAt = lastReadAt;
    }
}