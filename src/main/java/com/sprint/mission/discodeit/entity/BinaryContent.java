package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import java.util.UUID;

@Getter
public class BinaryContent {
    private UUID id;
    private String fileName;
    private String contentType; // MIME 타입 (예: "image/png", "application/json")
    private Long fileSize; // 파일 크기 (바이트 단위)
    private String bytes; // Base64 인코딩된 바이너리 데이터

    // 기본 생성자 (Jackson 역직렬화용)
    public BinaryContent() {
        this.id = UUID.randomUUID();
    }

    // 전체 필드를 받는 생성자
    public BinaryContent(String fileName, String contentType, Long fileSize, String bytes) {
        this.id = UUID.randomUUID();
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.bytes = bytes;
    }

    // ID만 받는 생성자 (파일 읽기 등 기존 객체 복원용)
    public BinaryContent(UUID id) {
        this.id = id;
    }
}