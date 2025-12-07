package com.sprint.mission.discodeit.entity;

import java.io.Serializable; // 직렬화를 위해 추가
import java.util.UUID;

public class Message implements Serializable { // Serializable 구현
    private static final long serialVersionUID = 1L; // 직렬화 버전 ID 추가

    // 공통 필드
    private final UUID id;
    private final Long createdAt;
    private Long updatedAt;

    // Message 고유 필드 및 관계 필드
    private String content;
    private final UUID userId;
    private final UUID channelId;

    public Message(String content, UUID userId, UUID channelId) {
        this.id = UUID.randomUUID();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;

        this.content = content;
        this.userId = userId;
        this.channelId = channelId;
    }

    // --- Getter 함수 정의 ---
    public UUID getId() { return id; }
    public Long getCreatedAt() { return createdAt; }
    public Long getUpdatedAt() { return updatedAt; }
    public String getContent() { return content; }
    public UUID getUserId() { return userId; }
    public UUID getChannelId() { return channelId; }

    // --- 필드를 수정하는 update 함수 정의 ---
    public void update(String content) {
        this.content = content;
        this.updatedAt = System.currentTimeMillis();
    }
}