package com.sprint.mission.discodeit.exception.binarycontent;

import com.sprint.mission.discodeit.exception.global.ErrorCode;
import java.util.Map;
import java.util.UUID;

/**
 * 조회 시도한 바이너리 컨텐츠를 찾을 수 없을 때 사용.
 */
public class BinaryContentNotFoundException extends BinaryContentException {

  public BinaryContentNotFoundException() {
    super(ErrorCode.BINARY_CONTENT_NOT_FOUND);
  }

  public BinaryContentNotFoundException(UUID binaryContentId) {
    super(ErrorCode.BINARY_CONTENT_NOT_FOUND, Map.of("binaryContentId", binaryContentId));
  }

  public BinaryContentNotFoundException(Map<String, Object> details) {
    super(ErrorCode.BINARY_CONTENT_NOT_FOUND, details);
  }

  public static BinaryContentNotFoundException withId(UUID binaryContentId) {
    return new BinaryContentNotFoundException(binaryContentId);
  }
}
