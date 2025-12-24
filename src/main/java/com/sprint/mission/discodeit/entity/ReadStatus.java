package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class ReadStatus implements Serializable {
    private static final long serialVersionUID = 1L;
    private final UUID id;
    private UUID uId;
    private UUID cId;
    private final Instant createdAt;
    private Instant modifiedAt;
    private Instant lastReadMessageAt;

    public ReadStatus(UUID uId, UUID cId, Instant lastReadMessageAt) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.uId = uId;
        this.cId = cId;;
        this.lastReadMessageAt = lastReadMessageAt;
    }

    public void update(Instant newLastReadAt) {
        boolean anyValueUpdated = false;
        if (newLastReadAt != null && !newLastReadAt.equals(this.lastReadMessageAt)) {
            this.lastReadMessageAt = newLastReadAt;
            anyValueUpdated = true;
        }

        if (anyValueUpdated) {
            this.modifiedAt = Instant.now();
        }
    }


}
