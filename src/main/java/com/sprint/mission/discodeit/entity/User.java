package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Getter // Lombok이 모든 필드의 Getter를 자동으로 생성합니다.
public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final UUID id;
    private String name;
    private String email;

    public User(String name, String email) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.email = email;
    }

    public void update(String name, String email) {
        this.name = name;
        this.email = email;
    }
}