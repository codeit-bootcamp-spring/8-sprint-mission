package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter // Lombok이 모든 필드의 Getter를 자동으로 생성합니다.
public class Message implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final UUID id;
    private final UUID authorId;
    private final UUID channelId;
    private String content;
    private final Instant createdAt;

    public Message(UUID authorId, UUID channelId, String content) {
        this.id = UUID.randomUUID();
        this.authorId = authorId;
        this.channelId = channelId;
        this.content = content;
        this.createdAt = Instant.now();
    }

    public void update(String content) {
        this.content = content;
    }
}