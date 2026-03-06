package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentSaveFailedException;
import jakarta.annotation.PreDestroy;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
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
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "s3")
@Component
public class S3BinaryContentStorage implements BinaryContentStorage {

  private final S3Properties s3Properties;
  private final S3Client s3Client;
  private final S3Presigner s3Presigner;

  public S3BinaryContentStorage(S3Properties s3Properties) {
    this.s3Properties = s3Properties;

    StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
        AwsBasicCredentials.create(s3Properties.getAccessKey(), s3Properties.getSecretKey()));
    Region s3Region = Region.of(s3Properties.getRegion());

    this.s3Client = S3Client.builder()
        .region(s3Region)
        .credentialsProvider(credentialsProvider)
        .build();

    this.s3Presigner = S3Presigner.builder()
        .region(s3Region)
        .credentialsProvider(credentialsProvider)
        .build();
  }

  @Override
  public UUID put(UUID binaryContentId, byte[] bytes) {
    String key = binaryContentId.toString();

    try {
      PutObjectRequest putObjectRequest = PutObjectRequest.builder()
          .bucket(s3Properties.getBucket())
          .key(key)
          .build();

      s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));

      return binaryContentId;
    } catch (Exception e) {
      throw new BinaryContentSaveFailedException();
    }
  }

  @Override
  public InputStream get(UUID binaryContentId) {
    String key = binaryContentId.toString();

    try {
      GetObjectRequest getObjectRequest = GetObjectRequest.builder()
          .bucket(s3Properties.getBucket())
          .key(key)
          .build();

      byte[] bytes = s3Client.getObjectAsBytes(getObjectRequest).asByteArray();
      return new ByteArrayInputStream(bytes);
    } catch (Exception e) {
      throw new BinaryContentNotFoundException(binaryContentId);
    }
  }

  @Override
  public ResponseEntity<?> download(BinaryContentDto file) {
    String key = file.id().toString();
    String contentType = file.contentType();
    String presignedUrl = generatePresignedUrl(key, contentType);

    HttpHeaders headers = new HttpHeaders();
    headers.setLocation(URI.create(presignedUrl));

    return new ResponseEntity<>(headers, HttpStatus.FOUND);
  }

  private String generatePresignedUrl(String key, String contentType) {
    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(s3Properties.getBucket())
        .key(key)
        .responseContentType(contentType)
        .build();

    GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
        .signatureDuration(Duration.ofSeconds(s3Properties.getPresignedUrlExpiration()))
        .getObjectRequest(getObjectRequest)
        .build();

    PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(
        getObjectPresignRequest);
    return presignedGetObjectRequest.url().toString();
  }

  @PreDestroy
  public void close() {
    if (s3Client != null) {
      s3Client.close();
    }
    if (s3Presigner != null) {
      s3Presigner.close();
    }
  }
}
