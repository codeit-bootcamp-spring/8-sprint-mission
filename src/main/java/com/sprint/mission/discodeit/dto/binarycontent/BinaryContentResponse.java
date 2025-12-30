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
    • authorId          : 프로필 이미지와 관련된 유저 id
    • messageId         : 메시지 첨부파일과 관련된 메시지 id
 */
public record BinaryContentResponse(
    UUID id,
    String fileName,
    String contentType,
    String base64Data,
    Instant createdAt,
    UUID authorId,
    UUID messageId
) {

}
