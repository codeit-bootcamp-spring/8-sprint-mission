package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Getter //  외부(Service)에서 데이터를 읽기 위해 필수
@NoArgsConstructor
public class User {
    private UUID id;
    private String name;
    private String email;
    private String password;
    private UUID profileId;

    public User(String name, String email, String password, UUID profileId) {
        this.id = UUID.randomUUID(); //  ID가 null이 되지 않도록 생성 시 할당
        this.name = name;
        this.email = email;
        this.password = password;
        this.profileId = profileId;
    }

    public void update(String name, String email, String password) {
        if (name != null) this.name = name;
        if (email != null) this.email = email;
        if (password != null) this.password = password;
    }

    public void updateProfileId(UUID profileId) {
        this.profileId = profileId;
    }
}