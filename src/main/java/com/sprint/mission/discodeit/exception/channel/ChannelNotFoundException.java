package com.sprint.mission.discodeit.exception.channel;

import com.sprint.mission.discodeit.exception.global.ErrorCode;
import java.util.Map;
import java.util.UUID;

/**
 * 조회·수정·삭제 시도한 채널을 찾을 수 없을 때 사용.
 * details에 channelId(조회 시도한 채널 ID) 등을 담을 수 있다.
 */
public class ChannelNotFoundException extends ChannelException {

  public ChannelNotFoundException() {
    super(ErrorCode.CHANNEL_NOT_FOUND);
  }

  public ChannelNotFoundException(UUID channelId) {
    super(ErrorCode.CHANNEL_NOT_FOUND, Map.of("channelId", channelId));
  }

  public ChannelNotFoundException(Map<String, Object> details) {
    super(ErrorCode.CHANNEL_NOT_FOUND, details);
  }
}
