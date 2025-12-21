package com.sprint.mission.discodeit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ReadStatusCreateRequest {
    private UUID userId;
    private UUID channelId;
    private UUID lastReadMessageId;
}