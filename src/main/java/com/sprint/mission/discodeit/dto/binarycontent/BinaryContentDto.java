package com.sprint.mission.discodeit.dto.binarycontent;

import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import java.util.UUID;

/*
    BinaryContentDto
    -------------------------
    저장 된 바이너리 파일 정보 조회 용 DTO

    [필드 설명]
    • id                : BinaryContent의 id
    • fileName          : 파일 이름
    • size              : 파일 사이즈
    • contentType       : 파일 타입
    • status            : 업로드 상태 (PROCESSING / SUCCESS / FAIL)
 */
public record BinaryContentDto(
    UUID id,
    String fileName,
    Long size,
    String contentType,
    BinaryContentStatus status
) {

}
