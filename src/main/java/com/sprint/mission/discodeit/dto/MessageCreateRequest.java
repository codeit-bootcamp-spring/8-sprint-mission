package com.sprint.mission.discodeit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MessageCreateRequest {
    private UUID authorId;
    private UUID channelId;
    private String content;
    private List<UUID> attachmentIds;
}