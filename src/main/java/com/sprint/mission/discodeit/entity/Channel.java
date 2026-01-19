package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Channel implements Serializable {
    private final UUID id;
    private String name;
    private String description;
    private ChannelType type;
    private UUID ownerId;
    private Set<UUID> memberIds;

    public Channel(String name, String description, ChannelType type, UUID ownerId) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.type = type;
        this.ownerId = ownerId;
        this.memberIds = new HashSet<>();
    }

    // --- Getter 메서드 ---
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public ChannelType getType() { return type; }
    public UUID getOwnerId() { return ownerId; }
    public Set<UUID> getMemberIds() { return memberIds; }

    // Service가 호출하는 상태 변경 메서드
    public void update(String newName, String newDescription) {
        if (newName != null) this.name = newName;
        if (newDescription != null) this.description = newDescription;
    }
}