package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class Channel {
    private UUID id;
    private String name;
    private String description;
    private ChannelType type;
    private UUID ownerId; //  소유자 정보는 채널의 속성이므로 유지

    //  private Set<UUID> memberIds = new HashSet<>(); -> 삭제 (ReadStatus에서 관리)

    public Channel(String name, String description) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.type = type;
        this.ownerId = ownerId;
    }

    public void update(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Set<UUID> getMemberIds() {
        return Set.of();
    }
}