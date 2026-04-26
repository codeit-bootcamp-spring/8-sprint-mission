package com.sprint.mission.discodeit.storage.s3;

import com.sprint.mission.discodeit.config.MDCLoggingInterceptor;
import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import com.sprint.mission.discodeit.event.S3UploadFailedEvent;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
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

		private static final String OPERATION_NAME = "S3BinaryContentStorage.put";

		private final String bucket;
		private final long presignedUrlExpiration;
		private final S3Client s3Client;
		private final S3Presigner s3Presigner;
		private final ApplicationEventPublisher applicationEventPublisher;

		public S3BinaryContentStorage(
				@Value("${discodeit.storage.s3.access-key}") String accessKey,
				@Value("${discodeit.storage.s3.secret-key}") String secretKey,
				@Value("${discodeit.storage.s3.region}") String region,
				@Value("${discodeit.storage.s3.bucket}") String bucket,
				@Value("${discodeit.storage.s3.presigned-url-expiration:600}") long presignedUrlExpiration,
				ApplicationEventPublisher applicationEventPublisher
		) {
				this.bucket = bucket;
				this.presignedUrlExpiration = presignedUrlExpiration;
				this.applicationEventPublisher = applicationEventPublisher;

				AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
				StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
						credentials);
				Region awsRegion = Region.of(region);

				this.s3Client = S3Client.builder()
						.region(awsRegion)
						.credentialsProvider(credentialsProvider)
						.build();

				this.s3Presigner = S3Presigner.builder()
						.region(awsRegion)
						.credentialsProvider(credentialsProvider)
						.build();
		}

		@Override
		@Retryable(
				retryFor = SdkException.class,
				maxAttempts = 4,
				backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 10_000)
		)
		public UUID put(UUID binaryContentId, byte[] bytes) {
				String key = binaryContentId.toString();
				s3Client.putObject(
						PutObjectRequest.builder()
								.bucket(bucket)
								.key(key)
								.contentLength((long) bytes.length)
								.build(),
						RequestBody.fromBytes(bytes)
				);
				return binaryContentId;
		}

		@Recover
		public UUID recoverPut(Throwable ex, UUID binaryContentId, byte[] bytes) {
				String requestId = resolveRequestIdForFailureNotice();
				applicationEventPublisher.publishEvent(new S3UploadFailedEvent(
						OPERATION_NAME,
						requestId,
						binaryContentId,
						bytes != null ? bytes.length : null,
						ex.getMessage() != null ? ex.getMessage() : ex.toString()
				));
				log.error(
						"S3 바이너리 업로드 최종 실패(재시도 소진), operation={}, requestId={}, binaryContentId={}, bytes={}, error={}",
						OPERATION_NAME,
						requestId,
						binaryContentId,
						bytes != null ? bytes.length : null,
						ex.getMessage(),
						ex);
				throw new RuntimeException("S3 put failed after retries for id " + binaryContentId, ex);
		}

		private static String resolveRequestIdForFailureNotice() {
				return Optional.ofNullable(MDC.get(MDCLoggingInterceptor.MDC_REQUEST_ID_FULL))
						.or(() -> Optional.ofNullable(MDC.get(MDCLoggingInterceptor.MDC_REQUEST_ID)))
						.orElse("(MDC에 요청 ID 없음)");
		}

		@Override
		public InputStream get(UUID binaryContentId) {
				return s3Client.getObject(
						GetObjectRequest.builder()
								.bucket(bucket)
								.key(binaryContentId.toString())
								.build()
				);
		}

		@Override
		public ResponseEntity<?> download(BinaryContentDto metaData) {
				String presignedUrl = generatePresignedUrl(metaData.id().toString(),
						metaData.contentType());
				return ResponseEntity
						.status(HttpStatus.FOUND)
						.location(URI.create(presignedUrl))
						.build();
		}

		private String generatePresignedUrl(String key, String contentType) {
				GetObjectRequest getObjectRequest = GetObjectRequest.builder()
						.bucket(bucket)
						.key(key)
						.build();

				GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
						.signatureDuration(Duration.ofSeconds(presignedUrlExpiration))
						.getObjectRequest(getObjectRequest)
						.build();

				PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
				return presignedRequest.url().toString();
		}
}
