package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.entity.ChannelType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BinaryContentCreatedEvent(
    BinaryContentDto binaryContentDto,
    byte[] data,
    BinaryContentType binaryContentType,
    ChannelType channelType,
    List<UUID> participantIds,
    Instant occurredAt
) {

  public static BinaryContentCreatedEvent now(
      BinaryContentDto binaryContentDto,
      byte[] data,
      BinaryContentType binaryContentType,
      ChannelType channelType,
      List<UUID> participantIds
  ) {
    return new BinaryContentCreatedEvent(
        binaryContentDto, data, binaryContentType, channelType, participantIds,
        Instant.now()
    );
  }
}
