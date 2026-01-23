package com.sprint.mission.discodeit.dto.binarycontent;

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
    • bytes             : 바이너리 데이터
 */
public record BinaryContentDto(
    UUID id,
    String fileName,
    Long size,
    String contentType
) {

}
