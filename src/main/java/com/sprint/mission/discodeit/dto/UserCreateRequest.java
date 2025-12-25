package com.sprint.mission.discodeit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor //  모든 필드를 받는 생성자 자동 생성
public class UserCreateRequest {
    private String name;
    private String email;
    private String password; // 멘토님 요청으로 추가된 필드
    private String fileName;
    private String fileType;
    private Long fileSize;
}