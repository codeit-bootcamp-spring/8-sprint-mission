package com.sprint.mission.discodeit.DTO.response;

import com.sprint.mission.discodeit.entity.BinaryContent;

import java.time.Instant;
import java.util.UUID;

public record BinaryContentResponse(
        UUID id,
        String fileName,
        String contentType,
        long size,
        byte[] bytes,
        Instant createdAt

) {
    public static BinaryContentResponse from(BinaryContent binaryContent) {

        return new BinaryContentResponse(
                binaryContent.getId(),
                binaryContent.getFileName(),
                binaryContent.getContentType(),
                binaryContent.getSize(),
                binaryContent.getBytes(),
                binaryContent.getCreatedAt()
        );
    }
}
