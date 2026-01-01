package com.sprint.mission.discodeit.DTO.request;

public record BinaryContentCreateRequest(
        String originalFileName,
        String savedName,
        String uploadPath,
        String contentType,
        byte[] bytes,
        String description
) {
}
