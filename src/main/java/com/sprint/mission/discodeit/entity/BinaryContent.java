package com.sprint.mission.discodeit.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import java.util.UUID;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BinaryContent {
    private UUID id;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private String bytes; // Base64 encoded file data

    public BinaryContent() {
        this.id = UUID.randomUUID();
    }

    public BinaryContent(String fileName, String contentType, Long fileSize, String bytes) {
        this.id = UUID.randomUUID();
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.bytes = bytes;
    }
}