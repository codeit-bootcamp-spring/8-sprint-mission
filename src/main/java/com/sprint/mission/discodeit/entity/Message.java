package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Getter
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final UUID userId;
    private final UUID channelId;

    private final Instant createdAt;
    private Instant modifiedAt;
    private final String channelName;
    private final String from;
    private String content;
    private List<UUID> attachmentIds;

    public Message(UUID userId, UUID channelId, String channelName, String from, String content, List<UUID> attachmentIds) {
        id = UUID.randomUUID();
        createdAt = Instant.now();
        modifiedAt = createdAt;

        this.userId = userId;
        this.channelId = channelId;
        this.channelName = channelName;
        this.from = from;
        this.content = content;
        this.attachmentIds = attachmentIds;
    }

    public String getFileName() {
        return id.toString().concat(".ser");
    }

    public void update(String content) {
        boolean anyValueUpdated = false;
        if(this.content != null && !this.content.equals(content)) {
            this.content = content;
            anyValueUpdated = true;
        }
        if(anyValueUpdated) {
            this.modifiedAt = Instant.now();
        }
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", uid=" + userId +
                ", cid=" + channelId +
                ", createdAt=" + createdAt +
                ", modifiedAt=" + modifiedAt +
                ", channelName='" + channelName + '\'' +
                ", from='" + from + '\'' +
                ", content='" + content + '\'' +
                ", attachmentIds=" + attachmentIds +
                '}';
    }
}
