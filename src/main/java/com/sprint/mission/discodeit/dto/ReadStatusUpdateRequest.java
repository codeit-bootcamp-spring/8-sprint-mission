package com.sprint.mission.discodeit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ReadStatusUpdateRequest {
    private UUID id; // 수정 대상 객체의 id
    private UUID lastReadMessageId; // 수정할 값
}