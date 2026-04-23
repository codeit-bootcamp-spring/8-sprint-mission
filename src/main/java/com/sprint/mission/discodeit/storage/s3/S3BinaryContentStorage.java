package com.sprint.mission.discodeit.storage.s3;

import com.sprint.mission.discodeit.config.MDCLoggingInterceptor;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserRole;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "s3")
public class S3BinaryContentStorage implements BinaryContentStorage {

  private final S3Properties properties;
  private final S3Client s3Client;
  private final S3Presigner s3Presigner;

  private final UserRepository userRepository;
  private final NotificationRepository notificationRepository;

  @Override
  @Retryable(
      retryFor = {Exception.class},    // 모든 예외 발생 시 재시도
      maxAttempts = 3,                 // 최대 3번 시도 (처음 시도 + 2번 재시도)
      backoff = @Backoff(delay = 2000) // 실패 시 2초 대기 후 다시 시도
  )
  public UUID put(UUID id, byte[] bytes) {
    log.info("[S3_STORAGE] 업로드 시도 - binaryContentId={}", id);
    String key = "storage/" + id.toString();

    PutObjectRequest request = PutObjectRequest.builder()
        .bucket(properties.bucket())
        .key(key)
        .build();

    s3Client.putObject(request, RequestBody.fromBytes(bytes));
    return id;
  }

  @Recover
  public UUID recover(Exception e, UUID id, byte[] bytes) {
    // 현재 스레드의 Request ID 획득 (TaskDecorator가 복사해준 값)
    String requestId = MDC.get(MDCLoggingInterceptor.MDC_REQUEST_ID);

    log.error("[RETRY_EXHAUSTED] S3 업로드 최종 실패. RequestId: {}, ContentId: {}, Error: {}",
        requestId, id, e.getMessage());

    // 알림을 받을 관리자들 조회
    List<User> admins = userRepository.findAllByRole(UserRole.ADMIN);

    // 요구사항에 부합하는 알림 내용 작성
    String title = "비동기 작업 실패 알림 (S3 업로드)";
    String content = String.format(
        "RequestId: %s\nBinaryContentId: %s\nError: %s",
        requestId != null ? requestId : "N/A",
        id,
        e.getMessage()
    );

    // 모든 관리자에게 알림 생성
    List<Notification> failureAlerts = admins.stream()
        .map(admin -> new Notification(admin, title, content))
        .toList();

    notificationRepository.saveAll(failureAlerts);

    return id; // 복구 완료 후 원래 ID 반환 (실패 기록은 DB에 남음)
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
