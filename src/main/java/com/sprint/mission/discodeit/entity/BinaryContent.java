package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class BinaryContent implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final String fileName;
    private final String fileType;
    private final Long size;
    private final Instant createdAt;
    // 수정 불가능하므로 updatedAt은 정의하지 않음

    public BinaryContent(String fileName, String fileType, Long size) {
        this.id = UUID.randomUUID();
        this.fileName = fileName;
        this.fileType = fileType;
        this.size = size;
        this.createdAt = Instant.now();
    }
}