package com.sprint.mission.discodeit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ReadStatusUpdateRequest {
    private UUID id; // 수정 대상 객체의 id
    private Instant newLastReadAt; // API 명세서에 맞춘 필드
    // 하위 호환성을 위한 필드
    private UUID lastReadMessageId;
}