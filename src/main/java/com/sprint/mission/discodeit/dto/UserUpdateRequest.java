package com.sprint.mission.discodeit.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class UserUpdateRequest {
    // API 명세서 필드
    private String newUsername;
    private String newEmail;
    private String newPassword;
    // 내부 사용을 위한 필드 (하위 호환성)
    private UUID id;
    private String name;
    private String password;
    private String profileImage;
    private UUID profileId;
    
    // 생성자 오버로드 (하위 호환성)
    public UserUpdateRequest(UUID id, String name, String password, String profileImage, UUID profileId) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.profileImage = profileImage;
        this.profileId = profileId;
    }
}