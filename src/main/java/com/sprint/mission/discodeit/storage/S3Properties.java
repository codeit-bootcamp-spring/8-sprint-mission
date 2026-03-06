package com.sprint.mission.discodeit.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "discodeit.storage.s3")
public class S3Properties {

  private String accessKey;
  private String secretKey;
  private String region;
  private String bucket;
  private Long presignedUrlExpiration;
}
