package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Getter // Lombok이 모든 필드의 Getter를 자동으로 생성합니다.
public class Channel implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final UUID id;
    private String name;
    private UUID ownerId;

    public Channel(String name, UUID ownerId) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.ownerId = ownerId;
    }

    // 서비스 레이어의 요구사항에 맞춰 파라미터 2개를 받는 update 메서드
    public void update(String name, UUID ownerId) {
        this.name = name;
        this.ownerId = ownerId;
    }
}