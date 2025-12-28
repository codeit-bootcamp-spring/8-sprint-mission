package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class User {
    private UUID id;
    private String name;
    private String email;
    private String password;
    //  멘토 피드백 반영: 다이어그램에 맞춰 profileId로 변경
    private UUID profileId;

    public User(String name, String email, String password, UUID profileId) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.email = email;
        this.password = password;
        this.profileId = profileId;
    }

    // 정보 수정 시에도 profileId(UUID)를 받도록 변경
    public void update(String name, String password, UUID profileId) {
        this.name = name;
        this.password = password;
        if (profileId != null) {
            this.profileId = profileId;
        }
    }

    public String getProfileImage() {
        return "";
    }
}