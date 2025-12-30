package com.sprint.mission.discodeit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BinaryContentCreateRequest {
    private String fileName;
    private String fileType; // MIME 타입 (예: "image/png", "application/json")
    private Long fileSize; // 파일 크기 (바이트 단위)
    private String bytes; // Base64 인코딩된 바이너리 데이터 (선택적)
}