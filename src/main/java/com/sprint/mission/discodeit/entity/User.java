package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.util.UUID;

public class User implements Serializable {
    private final UUID id;
    private String name;
    private String email;

    public User(String name, String email) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.email = email;
    }

    // --- Getter 메서드 유지 ---
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }

    //  Service가 호출하는 상태 변경 메서드 추가
    public void update(String newName, String newEmail) {
        this.name = newName;
        this.email = newEmail;
        // 일반적으로 Entity의 상태 변경은 Entity 내부에서만 일어나야 합니다.
    }

    // toString(), hashCode(), equals() 등 필요한 메서드는 유지
}