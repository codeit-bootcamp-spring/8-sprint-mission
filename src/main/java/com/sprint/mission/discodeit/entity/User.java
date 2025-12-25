package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Getter //  필수: getId(), getName() 등을 생성합니다.
@NoArgsConstructor
public class User {
    private UUID id;
    private String name;
    private String email;
    private String password;

    public User(String name, String email, String password) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public void update(String name, String password) {
        this.name = name;
        this.password = password;
    }
}