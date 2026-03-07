package com.sprint.mission.discodeit.storage.s3;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "s3")
public class S3BinaryContentStorage implements BinaryContentStorage {

  private final S3Properties properties;
  private final S3Client s3Client;
  private final S3Presigner s3Presigner;

  @Override
  public UUID put(UUID id, byte[] bytes) {
    String key = "storage/" + id.toString();

    PutObjectRequest request = PutObjectRequest.builder()
        .bucket(properties.bucket())
        .key(key)
        .build();

    s3Client.putObject(request, RequestBody.fromBytes(bytes));
    return id;
  }

  @Override
  public InputStream get(UUID id) {
    String key = "storage/" + id.toString();

    GetObjectRequest request = GetObjectRequest.builder()
        .bucket(properties.bucket())
        .key(key)
        .build();

    return s3Client.getObject(request);
  }

  @Override
  public ResponseEntity<?> download(BinaryContentDto dto) {

    String key = "storage/" + dto.id().toString();

    // PresignedUrl을 활용해 리다이렉트
    String presignedUrl = generatePresignedUrl(key, dto.contentType());

    return ResponseEntity.status(HttpStatus.SEE_OTHER)
        .location(URI.create(presignedUrl))
        .build();
  }

  private String generatePresignedUrl(String key, String contentType) {

    GetObjectRequest objectRequest = GetObjectRequest.builder()
        .bucket(properties.bucket())
        .key(key)
        .responseContentType(contentType)
        .build();

    GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
        .signatureDuration(Duration.ofSeconds(properties.presignedUrlExpiration()))
        .getObjectRequest(objectRequest)
        .build();

    return s3Presigner.presignGetObject(presignRequest).url().toString();
  }
}
