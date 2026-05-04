package com.sprint.mission.discodeit.dto.kafka;

import java.util.UUID;

public record S3UploadFailedKafkaEvent(
    UUID binaryContentId
) {
}
