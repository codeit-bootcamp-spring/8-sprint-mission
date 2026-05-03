package com.sprint.mission.discodeit.event;

import java.util.UUID;

/**
 * BinaryContent 메타 데이터가 DB에 저장되었음을 알리는 이벤트
 * binaryContentId - 저장된 메타데이터 식별자
 * bytes           - 실제 저장할 바이너리 데이터
 */
public record BinaryContentCreatedEvent(
    UUID binaryContentId,
    byte[] bytes
) {
}
