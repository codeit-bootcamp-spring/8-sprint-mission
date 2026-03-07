package com.sprint.mission.discodeit.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@EnableConfigurationProperties(S3Properties.class)
public class S3Config {

  @Bean
  public S3Client s3Client(S3Properties s3Properties) {
    AwsBasicCredentials credentials = AwsBasicCredentials.create(
        s3Properties.accessKey(),
        s3Properties.secretKey()
    );

    return S3Client.builder()
        .region(Region.of(s3Properties.region()))
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .build();
  }

  @Bean
  public S3Presigner s3Presigner(S3Properties s3Properties) {
    AwsBasicCredentials credentials = AwsBasicCredentials.create(
        s3Properties.accessKey(),
        s3Properties.secretKey()
    );

    return S3Presigner.builder()
        .region(Region.of(s3Properties.region()))
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .build();
  }

}
