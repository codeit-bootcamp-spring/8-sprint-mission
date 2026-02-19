package com.sprint.mission.discodeit.exception;

import java.util.Map;

public class PrivateChannelUpdatedException extends ChannelException {

  public PrivateChannelUpdatedException(Map<String, Object> details) {
    super(ErrorCode.PRIVATE_CHANNEL_UPDATE, details);
  }

}
