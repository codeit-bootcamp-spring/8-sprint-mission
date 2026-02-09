package com.sprint.mission.discodeit.exception;

import java.util.Map;
import java.util.UUID;

/*
  방금 저장된 BinaryContent가 DB에 없습니다 -> 현재는 BINARY_CONTENT_NOT_FOUND 코드 사용
  추후 리팩토링 과정에서 사용할 가능성 존재하여 코드 유지
 */
public class InternalErrorException extends DiscodeitException {

  public InternalErrorException(UUID id) {
    super(ErrorCode.INTERNAL_ERROR, Map.of("id", id));
  }
}
