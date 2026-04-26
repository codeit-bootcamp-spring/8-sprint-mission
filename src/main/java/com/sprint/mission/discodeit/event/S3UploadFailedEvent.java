package com.sprint.mission.discodeit.event;

import java.util.UUID;

public record S3UploadFailedEvent(
    String operationName,
    String requestId,
    UUID binaryContentId,
    Integer byteLength,
    String errorMessage
) {
}
