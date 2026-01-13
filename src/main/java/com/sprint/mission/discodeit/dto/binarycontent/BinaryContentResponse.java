package com.sprint.mission.discodeit.dto.binarycontent;

import java.time.Instant;
import java.util.UUID;

/*
    BinaryContentDto
    -------------------------
    저장 된 바이너리 파일 정보 조회 용 DTO

    [필드 설명]
    • id                : BinaryContent의 id
    • fileName          : 파일 이름
    • createdAt         : 파일 생성 시각
 */
public record BinaryContentResponse(
    UUID id,
    String fileName,
    String contentType,
    byte[] bytes,
    Instant createdAt
) {

}
