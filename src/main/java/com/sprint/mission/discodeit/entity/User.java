package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.NoArgsConstructor; // 추가
import java.util.UUID;

@Getter
@NoArgsConstructor //  JSON 파일에서 데이터를 읽어올 때 필수입니다.
public class User {
    private UUID id;
    private String name;
    private String email;
    private String password;
    private UUID profileId;

    public User(String name, String email, String password, UUID profileId) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.email = email;
        this.password = password;
        this.profileId = profileId;
    }

    public void update(String name, String email) {
        this.name = name;
        this.email = email;
    }
}