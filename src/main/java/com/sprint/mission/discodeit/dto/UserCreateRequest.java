package com.sprint.mission.discodeit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor //  모든 필드를 받는 생성자 자동 생성
public class UserCreateRequest {
    @JsonProperty("username")
    private String username;
    private String email;
    private String password; // 멘토님 요청으로 추가된 필드
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String profileImage; // Base64 encoded image data
    
    // name 필드에 대한 getter (기존 코드 호환성을 위해)
    public String getName() {
        return username;
    }
    
    // Setter 메서드 추가 (프로필 이미지 설정용)
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    
    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}