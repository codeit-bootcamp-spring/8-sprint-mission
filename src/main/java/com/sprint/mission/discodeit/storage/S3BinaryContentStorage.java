package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.dto.BinaryContentDto;
import com.sprint.mission.discodeit.config.S3Properties;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;

import com.sprint.mission.discodeit.event.S3UploadFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "s3")
public class S3BinaryContentStorage implements BinaryContentStorage {

    // S3 core Infrastructure
    private final S3Properties s3Properties;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    // Infrastructure Service
    private final ApplicationEventPublisher applicationEventPublisher;

    // S3Exception.class 발생하면 재시도
    // 처음 딜레이 시간은 1초, 이후로 2배씩 늘어난다.
    @Retryable(
            retryFor = S3Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @Override
    public UUID put(UUID id, byte[] bytes) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(s3Properties.bucket())
                .key(id.toString())
                .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));
        return id;
    }

    @Override
    public InputStream get(UUID id) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3Properties.bucket())
                .key(id.toString())
                .build();
        return s3Client.getObject(getObjectRequest);
    }

    @Override
    public ResponseEntity<Void> download(BinaryContentDto binaryContentDto) {
        String key = binaryContentDto.id().toString();

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3Properties.bucket())
                .key(key)
                .responseContentType(binaryContentDto.contentType())
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .getObjectRequest(getObjectRequest)
                .build();

        String presignedUrl = s3Presigner.presignGetObject(presignRequest).url().toString();

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(presignedUrl))
                .build();
    }

    @Override
    public void delete(UUID id) {
        try {
            s3Client.deleteObject(d -> d.bucket(s3Properties.bucket()).key(id.toString()));
            log.info("S3 객체 삭제 완료: {}", id);
        } catch (S3Exception e) {
            log.error("S3 객체 삭제 실패: {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "S3 객체 삭제 중 오류가 발생했습니다.",
                    e);
        }
    }

    // Retry의 재시도 횟수가 끝나면 호출된다.
    @Recover
    public void recover(S3Exception e, UUID binaryContentId, byte[] bytes) {
        log.error("S3 업로드 재시도 실패: {}, id: {}", e.getMessage(), binaryContentId);
        applicationEventPublisher.publishEvent(
                new S3UploadFailedEvent(binaryContentId, e)
        );

        throw new RuntimeException(e);
    }

}
