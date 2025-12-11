package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class Message implements Serializable {
    private final UUID id;
    private final UUID senderId;
    private final UUID channelId;
    private String content; // 메시지 내용
    private final LocalDateTime timestamp; // 메시지 전송 시간

    public Message(UUID senderId, UUID channelId, String content) {
        this.id = UUID.randomUUID();
        this.senderId = senderId;
        this.channelId = channelId;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    // --- Getter 메서드 유지 ---
    public UUID getId() { return id; }
    public UUID getSenderId() { return senderId; }
    public UUID getChannelId() { return channelId; }
    public String getContent() { return content; }
    public LocalDateTime getTimestamp() { return timestamp; }

    //  Service가 호출하는 상태 변경 메서드 추가
    public void update(String newContent) {
        this.content = newContent;
        // 메시지 내용만 수정 가능하도록 가정
    }

    // toString(), hashCode(), equals() 등 필요한 메서드는 유지
}