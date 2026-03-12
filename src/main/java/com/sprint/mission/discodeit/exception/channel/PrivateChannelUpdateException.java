package com.sprint.mission.discodeit.exception.channel;

import com.sprint.mission.discodeit.exception.global.ErrorCode;
import java.util.Map;
import java.util.UUID;

/**
 * 비공개 채널에 대한 수정 시도를 할 때 사용.
 * details에 channelId(업데이트 시도한 PRIVATE 채널의 ID) 등을 담을 수 있다.
 */
public class PrivateChannelUpdateException extends ChannelException {

  public PrivateChannelUpdateException() {
    super(ErrorCode.PRIVATE_CHANNEL_UPDATE);
  }

  public PrivateChannelUpdateException(UUID channelId) {
    super(ErrorCode.PRIVATE_CHANNEL_UPDATE, Map.of("channelId", channelId));
  }

  public PrivateChannelUpdateException(Map<String, Object> details) {
    super(ErrorCode.PRIVATE_CHANNEL_UPDATE, details);
  }

  public static PrivateChannelUpdateException forChannel(UUID channelId) {
    return new PrivateChannelUpdateException(channelId);
  }
}
