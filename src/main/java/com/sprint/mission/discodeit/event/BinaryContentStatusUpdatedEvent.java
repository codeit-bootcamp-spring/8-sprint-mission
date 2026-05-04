package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import java.time.Instant;
import lombok.Getter;

@Getter
public class BinaryContentStatusUpdatedEvent extends UpdatedEvent<BinaryContentStatus> {

  private final BinaryContentDto binaryContent;

  public BinaryContentStatusUpdatedEvent(BinaryContentStatus from, BinaryContentStatus to,
      Instant updatedAt, BinaryContentDto binaryContent) {
    super(from, to, updatedAt);
    this.binaryContent = binaryContent;
  }
}
