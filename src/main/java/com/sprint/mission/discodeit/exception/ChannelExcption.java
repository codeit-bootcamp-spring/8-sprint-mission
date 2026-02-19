package com.sprint.mission.discodeit.exception;

import java.util.Map;
import java.util.UUID;

public abstract class ChannelExcption extends DiscodeitException {

  protected ChannelExcption(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

  public static class PrivateChannelModificationNotAllowedException extends ChannelExcption {

    public PrivateChannelModificationNotAllowedException(UUID channelId) {
      super(ErrorCode.PRIVATE_CHANNEL_MODIFICATION_NOT_ALLOWED, Map.of("channelId", channelId));
    }
  }

  public static class ChannelNotFoundException extends ChannelExcption {

    public ChannelNotFoundException(UUID channelId) {
      super(ErrorCode.CHANNEL_NOT_FOUND, Map.of("channelId", channelId));
    }
  }
}
