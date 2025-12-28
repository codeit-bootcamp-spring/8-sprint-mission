package com.sprint.mission.discodeit.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class MessageResponse {
    private UUID id;
    private UUID authorId;
    private UUID channelId;
    private String content;
    private List<UUID> attachmentIds;
    private Instant createdAt;
    private Instant updatedAt;
}