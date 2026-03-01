package com.sprint.mission.discodeit.exception.readstatus;

import com.sprint.mission.discodeit.exception.global.ErrorCode;
import java.util.Map;
import java.util.UUID;

/**
 * 조회 시도한 읽음 상태를 찾을 수 없을 때 사용.
 */
public class ReadStatusNotFoundException extends ReadStatusException {

  public ReadStatusNotFoundException() {
    super(ErrorCode.READ_STATUS_NOT_FOUND);
  }

  public ReadStatusNotFoundException(UUID readStatusId) {
    super(ErrorCode.READ_STATUS_NOT_FOUND, Map.of("readStatusId", readStatusId));
  }

  public ReadStatusNotFoundException(Map<String, Object> details) {
    super(ErrorCode.READ_STATUS_NOT_FOUND, details);
  }

  public static ReadStatusNotFoundException withId(UUID readStatusId) {
    return new ReadStatusNotFoundException(readStatusId);
  }
}
