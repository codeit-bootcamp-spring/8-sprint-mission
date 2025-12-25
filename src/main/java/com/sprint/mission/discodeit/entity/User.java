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
    private String profileImage; // 추가: 프로필 이미지 파일명 또는 경로

    public User(String name, String email, String password) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.email = email;
        this.password = password;
    }

    // 멘토 피드백 반영: 프로필 이미지를 포함한 업데이트 메서드
    public void update(String name, String password, String profileImage) {
        this.name = name;
        this.password = password;
        if (profileImage != null) {
            this.profileImage = profileImage;
        }
    }
}