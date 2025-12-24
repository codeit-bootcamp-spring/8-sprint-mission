package com.sprint.mission.discodeit.DTO.response;

import com.sprint.mission.discodeit.entity.Message;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        UUID uid,
        UUID cid,
        String channelName,
        String from,
        String content,
        List<UUID> attachmentIds,
        Instant createdAt,
        Instant modifiedAt

) {
    public static MessageResponse from(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getUid(),
                message.getCid(),
                message.getChannelName(),
                message.getFrom(),
                message.getContent(),
                message.getAttachmentIds(),
                message.getCreatedAt(),
                message.getModifiedAt()
        );
    }
}
