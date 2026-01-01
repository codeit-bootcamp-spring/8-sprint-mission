package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class BinaryContent implements Serializable {
    private static final long serialVersionUID = 1L;
    private final UUID id;
    private final Instant createdAt;
    private final String originalFileName;
    private final String savedName;
    private final String uploadPath;
    private final String contentType;
    private final byte[] bytes;
    private final String description;


    public BinaryContent(String originalFileName, String savedName, String uploadPath, String contentType, byte[] bytes, String description) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.originalFileName = originalFileName;
        this.savedName = savedName;
        this.uploadPath = uploadPath;
        this.contentType = contentType;
        this.bytes = bytes;
        this.description = description;
    }
}
