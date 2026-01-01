package com.sprint.mission.discodeit.DTO.response;

import com.sprint.mission.discodeit.entity.BinaryContent;

import java.time.Instant;
import java.util.UUID;

public record BinaryContentResponse(
        String originalFileName,
        String savedName,
        String uploadPath,
        String contentType,
        byte[] bytes,
        String description,
        Instant createdAt
) {
    public static BinaryContentResponse from(BinaryContent binaryContent) {

        return new BinaryContentResponse(
                binaryContent.getOriginalFileName(),
                binaryContent.getSavedName(),
                binaryContent.getUploadPath(),
                binaryContent.getContentType(),
                binaryContent.getBytes(),
                binaryContent.getDescription(),
                binaryContent.getCreatedAt()
        );
    }
}
