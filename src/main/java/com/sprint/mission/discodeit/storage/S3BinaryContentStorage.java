package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.DTO.dto.BinaryContentDto;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Component
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "s3")
public class S3BinaryContentStorage implements BinaryContentStorage {

  private String accessKey;
  private String secretKey;
  private String region;
  private String bucket;

  public S3BinaryContentStorage(
      @Value("${discodeit.storage.s3.access-key}") String accessKey,
      @Value("${discodeit.storage.s3.secret-key}") String secretKey,
      @Value("${discodeit.storage.s3.region}") String region,
      @Value("${discodeit.storage.s3.bucket}") String bucket
  ) {
    this.accessKey = accessKey;
    this.secretKey = secretKey;
    this.region = region;
    this.bucket = bucket;
  }

  private S3Client getS3Client() {
    AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
    return S3Client.builder()
        .region(Region.of(region))
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .build();
  }

  private String generatePresignedUrl(String key, String contentType) {
    AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
    try (S3Presigner presigner = S3Presigner.builder()
        .region(Region.of(region))
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .build()) {
      GetObjectRequest getObjectRequest = GetObjectRequest.builder()
          .bucket(bucket)
          .key(key)
          .responseContentType(contentType)
          .build();

      GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
          .signatureDuration(Duration.ofMinutes(10))
          .getObjectRequest(getObjectRequest)
          .build();

      return presigner.presignGetObject(presignRequest).url().toString();
    }
  }

  @Override
  public UUID put(UUID id, byte[] bytes) {
    S3Client s3Client = getS3Client();
    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucket)
        .key(id.toString())
        .build();
    s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));
    return id;
  }

  @Override
  public InputStream get(UUID id) {
    S3Client s3Client = getS3Client();
    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(bucket)
        .key(id.toString())
        .build();
    return s3Client.getObject(getObjectRequest);
  }

  @Override
  public ResponseEntity<Void> download(BinaryContentDto binaryContentDto) {
    String key = binaryContentDto.id().toString();
    String presignedUrl = generatePresignedUrl(key, binaryContentDto.contentType());

    return ResponseEntity.status(HttpStatus.FOUND)
        .location(URI.create(presignedUrl))
        .build();
  }

  @Override
  public void delete(UUID id) {

  }
}
