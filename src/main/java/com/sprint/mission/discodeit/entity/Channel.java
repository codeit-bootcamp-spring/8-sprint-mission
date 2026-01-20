package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class Channel implements Serializable {
    private UUID id;
    private String name;
    private String description;
    private ChannelType type;
    private UUID ownerId;
    private Set<UUID> memberIds;
    private Instant createdAt;

    public Channel(String name, String description, ChannelType type, UUID ownerId) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.type = type;
        this.ownerId = ownerId;
        this.memberIds = new HashSet<>();
        this.createdAt = Instant.now();
    }

    // Service가 호출하는 상태 변경 메서드
    public void update(String newName, String newDescription) {
        if (newName != null) this.name = newName;
        if (newDescription != null) this.description = newDescription;
    }
}