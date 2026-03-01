package com.sprint.mission.discodeit.storage.s3;

import com.sprint.mission.discodeit.dto.datafix.BinaryContentDto;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
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
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "s3")
@Component
public class S3BinaryContentStorage implements BinaryContentStorage {

		private final String bucket;
		private final long presignedUrlExpiration;
		private final S3Client s3Client;
		private final S3Presigner s3Presigner;

		public S3BinaryContentStorage(
				@Value("${discodeit.storage.s3.access-key}") String accessKey,
				@Value("${discodeit.storage.s3.secret-key}") String secretKey,
				@Value("${discodeit.storage.s3.region}") String region,
				@Value("${discodeit.storage.s3.bucket}") String bucket,
				@Value("${discodeit.storage.s3.presigned-url-expiration:600}") long presignedUrlExpiration
		) {
				this.bucket = bucket;
				this.presignedUrlExpiration = presignedUrlExpiration;

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

		private S3Client getS3Client() {
				return s3Client;
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
