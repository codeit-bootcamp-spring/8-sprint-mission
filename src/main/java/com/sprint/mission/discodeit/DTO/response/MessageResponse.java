package com.sprint.mission.discodeit.DTO.response;

import com.sprint.mission.discodeit.entity.Message;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MessageResponse(
    UUID id,
    String content,
    UUID channelId,
    UUID authorId,
    List<UUID> attachmentIds,
    Instant createdAt,
    Instant modifiedAt

) {

  public static MessageResponse from(Message message) {
    return new MessageResponse(
        message.getId(),
        message.getContent(),
        message.getChannelId(),
        message.getAuthorId(),
        message.getAttachmentIds(),
        message.getCreatedAt(),
        message.getUpdatedAt()
    );
  }
}
