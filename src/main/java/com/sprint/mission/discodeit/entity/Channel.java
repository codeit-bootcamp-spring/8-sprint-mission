package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
public class Channel {
    private UUID id;
    private String name;
    private String type; // "PUBLIC" 또는 "PRIVATE"
    private UUID ownerId;
    private Set<UUID> memberIds = new HashSet<>(); // 멤버 ID 셋 추가

    // 기본 생성자 (Jackson용)
    public Channel() {}

    public Channel(String name, String type, UUID ownerId) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.type = type;
        this.ownerId = ownerId;
        this.memberIds.add(ownerId); // 소유자는 자동으로 멤버에 포함
    }

    public void addMember(UUID userId) {
        this.memberIds.add(userId);
    }

    public void update(String name) { // 파라미터 개수 서비스와 일치시킴
        this.name = name;
    }
}