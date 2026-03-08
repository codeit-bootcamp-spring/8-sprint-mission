package com.sprint.mission.discodeit.exception.readstatus;

import com.sprint.mission.discodeit.exception.global.ErrorCode;
import java.util.UUID;

public class DuplicateReadStatusException extends ReadStatusException {

  public DuplicateReadStatusException() {
    super(ErrorCode.READ_STATUS_ALREADY_EXISTS);
  }

  public static DuplicateReadStatusException forUserIdAndChannelId(UUID userId, UUID channelId) {
    return new DuplicateReadStatusException();
  }
}
