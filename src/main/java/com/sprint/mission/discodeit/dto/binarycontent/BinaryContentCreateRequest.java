package com.sprint.mission.discodeit.dto.binarycontent;

/*
    BinaryContentCreateRequest
    -------------------------
    바이너리 파일(이미지 및 첨부파일)을 생성 시 사용하는 DTO

    [필드 설명]
    • fileName          : 파일 이름
    • data              : 파일 데이터를 byte 배열로 표현
 */
public record BinaryContentCreateRequest(
        String fileName,
        byte[] data
) {
}
