package com.sprint.mission.discodeit.entity;

import java.io.Serializable; // 직렬화를 위해 추가
import java.util.UUID;

public class User implements Serializable { // Serializable 구현
    private static final long serialVersionUID = 1L; // 직렬화 버전 ID 추가

    // 공통 필드
    private final UUID id;
    private final Long createdAt;
    private Long updatedAt;

    // User 고유 필드
    private String name;
    private String email;

    public User(String name, String email) {
        this.id = UUID.randomUUID();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;

        this.name = name;
        this.email = email;
    }

    // --- Getter 함수 정의 ---
    public UUID getId() { return id; }
    public Long getCreatedAt() { return createdAt; }
    public Long getUpdatedAt() { return updatedAt; }
    public String getName() { return name; }
    public String getEmail() { return email; }

    // --- 필드를 수정하는 update 함수 정의 ---
    public void update(String name, String email) {
        this.name = name;
        this.email = email;
        this.updatedAt = System.currentTimeMillis();
    }
}