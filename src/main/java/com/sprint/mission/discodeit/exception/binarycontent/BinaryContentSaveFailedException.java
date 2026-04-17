package com.sprint.mission.discodeit.exception.binarycontent;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class BinaryContentSaveFailedException extends BinaryContentException {

  public BinaryContentSaveFailedException(UUID binaryContentId, String fileName) {
    super(ErrorCode.BINARY_CONTENT_SAVE_FAILED,
        Map.of("binaryContentId", binaryContentId, "fileName", fileName));
  }

  public BinaryContentSaveFailedException() {
    super(ErrorCode.BINARY_CONTENT_CONVERT_FAILED);
  }

  public BinaryContentSaveFailedException(Throwable cause) {
    super(ErrorCode.BINARY_CONTENT_SAVE_FAILED, cause);
  }
}
