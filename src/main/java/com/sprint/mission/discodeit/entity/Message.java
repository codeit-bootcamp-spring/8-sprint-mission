package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
public class Message implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final UUID authorId;
    private final UUID channelId;
    private String content;
    private List<UUID> attachmentIds; // 관계도에 따른 첨부파일 ID 리스트 추가
    private final Instant createdAt;
    private Instant updatedAt;

    public Message(UUID authorId, UUID channelId, String content, List<UUID> attachmentIds) {
        this.id = UUID.randomUUID();
        this.authorId = authorId;
        this.channelId = channelId;
        this.content = content;
        this.attachmentIds = attachmentIds;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void update(String content, List<UUID> attachmentIds) {
        this.content = content;
        this.attachmentIds = attachmentIds;
        this.updatedAt = Instant.now();
    }
}