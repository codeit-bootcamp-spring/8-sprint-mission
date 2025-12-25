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
    private String profileImage;

    // 기존 생성자 보완
    public User(String name, String email, String password) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.email = email;
        this.password = password;
        this.profileImage = "default.png"; // 기본값 설정
    }

    //  [오류 해결] 비어있던 생성자를 아래와 같이 채워주세요!
    public User(String name, String email, String password, String profileImage) {
        this.id = UUID.randomUUID(); // 고유 ID 생성 (이게 없어서 에러가 났습니다)
        this.name = name;
        this.email = email;
        this.password = password;
        this.profileImage = (profileImage != null) ? profileImage : "default.png"; //
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