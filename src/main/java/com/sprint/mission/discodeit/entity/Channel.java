package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.util.UUID;

public class Channel implements Serializable {
    private final UUID id;
    private String name;
    private UUID ownerId; // 채널 소유자 ID

    public Channel(String name, UUID ownerId) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.ownerId = ownerId;
    }

    // --- Getter 메서드 유지 ---
    public UUID getId() { return id; }
    public String getName() { return name; }
    public UUID getOwnerId() { return ownerId; }

    //  Service가 호출하는 상태 변경 메서드 추가
    public void update(String newName, UUID newOwnerId) {
        this.name = newName;
        this.ownerId = newOwnerId;
    }

    // toString(), hashCode(), equals() 등 필요한 메서드는 유지
}