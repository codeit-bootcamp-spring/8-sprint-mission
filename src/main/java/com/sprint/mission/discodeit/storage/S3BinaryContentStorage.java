package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentSaveFailedException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import jakarta.annotation.PreDestroy;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Slf4j
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "s3")
@Component
public class S3BinaryContentStorage implements BinaryContentStorage {

  private final S3Properties s3Properties;
  private final S3Client s3Client;
  private final S3Presigner s3Presigner;
  private final UserRepository userRepository;
  private final NotificationService notificationService;
  private final String adminUsername;

  public S3BinaryContentStorage(S3Properties s3Properties,
      UserRepository userRepository,
      NotificationService notificationService,
      @Value("${admin.username}") String adminUsername
  ) {
    this.s3Properties = s3Properties;
    this.userRepository = userRepository;
    this.notificationService = notificationService;
    this.adminUsername = adminUsername;

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

  @Retryable(
      // 특정 예외일때만 재시도(리소스 낭비 줄이기 위함)
      retryFor = {BinaryContentSaveFailedException.class, SdkException.class, IOException.class},
      // 재시도 횟수
      maxAttempts = 3,
      // 재시도 간격 설정
      backoff = @Backoff(delay = 2000)
  )
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
      throw new BinaryContentSaveFailedException(e);
    }
  }

  /*
   * 재시도가 모두 실패했을 때 실행될 메소드
   * 첫번째 파라미터: 실패 예외 타입
   * 뒤 파라미터: @Retryable 메소드의 파라미터 순서와 동일
   * put이 3번 실패하고 실행되기 때문에 트랜잭션은 실패 상태
   * 알림을 recover에서 save하려고 해도 db에 반영이 안됨
   * 따라서 service에서 새로운 트랜잭션의 save 메소드 호출
   * */
  @Recover
  public UUID recover(BinaryContentSaveFailedException e,
      UUID binaryContentId,
      byte[] bytes
  ) {
    log.error("[S3BinaryContentStorage] S3 바이너리 저장 모두 실패! binaryContentId={}, error={}",
        binaryContentId, e.getMessage());

    User receiver = userRepository.findByUsername(adminUsername)
        .orElseThrow(() -> new UserNotFoundException(adminUsername));

    String title = "S3 파일 업로드 실패";
    String requestId = MDC.get("requestId");

    Notification notification = new Notification(
        receiver,
        title,
        String.format("RequestId: %s\n BinaryContentId: %s\n Error: %s", requestId, binaryContentId,
            e.getCause().getMessage())
    );

    notificationService.send(notification);

    log.info("[S3BinaryContentStorage] S3 바이너리 저장 실패 알림 관리자에게 전달");
    return null;
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
