package com.sprint.mission.discodeit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "discodeit.storage.s3")
public record S3Properties(
    String accessKey,
    String secretKey,
    String region,
    String bucket
) {

}
