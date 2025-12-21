package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import java.util.UUID;

@Getter
public class User {
    private UUID id;
    private String name;
    private String email;
    private UUID profileId;

    public User() {}

    public User(String name, String email, UUID profileId) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.email = email;
        this.profileId = profileId;
    }

    public void update(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // profileId까지 업데이트가 필요한 경우를 위한 오버로딩
    public void update(String name, String email, UUID profileId) {
        this.name = name;
        this.email = email;
        this.profileId = profileId;
    }
}