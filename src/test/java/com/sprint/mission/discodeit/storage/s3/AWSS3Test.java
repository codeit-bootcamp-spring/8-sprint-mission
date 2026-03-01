package com.sprint.mission.discodeit.storage.s3;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

public class AWSS3Test {

		private String bucketName;
		private S3Client s3Client;
		private S3Presigner s3Presigner;

		@BeforeEach
		void setUp() throws IOException {
				Properties props = new Properties();
				try (FileInputStream fis = new FileInputStream(".env")) {
						props.load(fis);
				}

				String accessKey = props.getProperty("AWS_S3_ACCESS_KEY").trim();
				String secretKey = props.getProperty("AWS_S3_SECRET_KEY").trim();
				String regionStr = props.getProperty("AWS_S3_REGION").trim();
				this.bucketName = props.getProperty("AWS_S3_BUCKET").trim();

				AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
				Region region = Region.of(regionStr);

				this.s3Client = S3Client.builder()
						.region(region)
						.credentialsProvider(StaticCredentialsProvider.create(credentials))
						.build();

				this.s3Presigner = S3Presigner.builder()
						.region(region)
						.credentialsProvider(StaticCredentialsProvider.create(credentials))
						.build();
		}

		@Test
		@DisplayName("S3 파일 업로드 테스트")
		void uploadTest() {
				s3Client.putObject(
						PutObjectRequest.builder()
								.bucket(bucketName)
								.key("test-file.txt")
								.contentType("text/plain")
								.build(),
						RequestBody.fromString("Hello S3!")
				);
				System.out.println("업로드 성공!");
		}

		@Test
		@DisplayName("S3 파일 다운로드 테스트")
		void downloadTest() {
				java.io.File file = new java.io.File("downloaded-test.txt");

				// 1. 기존에 같은 이름의 파일이 있다면 삭제 (FileAlreadyExistsException 방지)
				if (file.exists()) {
						file.delete();
				}

				s3Client.getObject(
						GetObjectRequest.builder()
								.bucket(bucketName)
								.key("test-file.txt")
								.build(),
						Paths.get("downloaded-test.txt")
				);
				System.out.println("다운로드 성공! → downloaded-test.txt");
		}

		@Test
		@DisplayName("Presigned URL 생성 테스트")
		void presignedUrlTest() {
				GetObjectRequest getObjectRequest = GetObjectRequest.builder()
						.bucket(bucketName)
						.key("test-file.txt")
						.build();

				GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
						.signatureDuration(Duration.ofMinutes(10))
						.getObjectRequest(getObjectRequest)
						.build();

				PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
				System.out.println("생성된 Presigned URL: " + presignedRequest.url());
		}
}
