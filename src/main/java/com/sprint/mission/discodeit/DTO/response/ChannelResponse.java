package com.sprint.mission.discodeit.DTO.response;

import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Channel;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record ChannelResponse(
    UUID id,
    ChannelType type,
    String name,
    String description,
    List<UUID> participantIds,
    Instant lastMessageAt,
    Instant createdAt,
    Instant updatedAt
) {

  public static ChannelResponse ofPublic(
      UUID id,
      ChannelType type,
      String name,
      String description,
      Instant lastMessageAt,
      Instant createdAt,
      Instant updatedAt
  ) {
    return new ChannelResponse(id, ChannelType.PUBLIC, name, description, List.of(), lastMessageAt,
        createdAt, updatedAt);
  }

  public static ChannelResponse ofPrivate(
      UUID id,
      ChannelType type,
      List<UUID> participantIds,
      Instant lastMessageAt,
      Instant createdAt,
      Instant updatedAt
  ) {

    List<UUID> nullOfParticipantUserIds =
        (participantIds != null) ? participantIds : List.of();

    return new ChannelResponse(id, ChannelType.PRIVATE, null, null, nullOfParticipantUserIds,
        lastMessageAt, createdAt, updatedAt);
  }

  public static ChannelResponse from(Channel channel, Instant lastMessageAt,
      List<UUID> participantIds) {

    return switch (channel.getType()) {
      case PUBLIC -> ofPublic(
          channel.getId(),
          channel.getType(),
          channel.getName(),
          channel.getDescription(),
          lastMessageAt,
          channel.getCreatedAt(),
          channel.getUpdatedAt()
      );
      case PRIVATE -> ofPrivate(
          channel.getId(),
          channel.getType(),
          participantIds,
          lastMessageAt,
          channel.getCreatedAt(),
          channel.getUpdatedAt()
      );
    };
  }
}
