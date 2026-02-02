package com.sprint.mission.discodeit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.UUID;

/**
 * [심화 요구사항] 사용자 정보 전송 객체
 * API 명세서에 맞춰 profile 필드 추가
 */
@Getter
@Builder
@AllArgsConstructor
public class UserDto {
    private UUID id;
    private String username;
    private String email;
    private BinaryContentDto profile;
    private boolean online;
}