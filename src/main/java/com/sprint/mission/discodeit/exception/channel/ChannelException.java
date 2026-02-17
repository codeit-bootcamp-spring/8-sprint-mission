package com.sprint.mission.discodeit.exception.channel;

import com.sprint.mission.discodeit.exception.global.DiscodeitException;
import com.sprint.mission.discodeit.exception.global.ErrorCode;
import java.util.Map;

/**
 * 채널 도메인 예외의 공통 부모.
 * 예외 계층 구조를 명확히 하기 위한 클래스이며, 직접 던지기보다 구체 예외를 사용한다.
 */
public class ChannelException extends DiscodeitException {

  public ChannelException(ErrorCode errorCode) {
    super(errorCode);
  }

  public ChannelException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

  public ChannelException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public ChannelException(ErrorCode errorCode, Map<String, Object> details, Throwable cause) {
    super(errorCode, details, cause);
  }
}
