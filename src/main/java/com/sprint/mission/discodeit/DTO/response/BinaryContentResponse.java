package com.sprint.mission.discodeit.DTO.response;

import com.sprint.mission.discodeit.entity.BinaryContent;

import java.time.Instant;

public record BinaryContentResponse(
    String fileName,
    Long size,
    String contentType,
    byte[] bytes,
    Instant createdAt
) {

  public static BinaryContentResponse from(BinaryContent binaryContent) {

    return new BinaryContentResponse(
        binaryContent.getFileName(),
        binaryContent.getSize(),
        binaryContent.getContentType(),
        binaryContent.getBytes(),
        binaryContent.getCreatedAt()
    );
  }
}
