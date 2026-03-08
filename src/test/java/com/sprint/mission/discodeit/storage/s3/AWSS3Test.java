package com.sprint.mission.discodeit.storage.s3;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Properties;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
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

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AWSS3Test {

    private String bucketName;
    private S3Client s3Client;
    private S3Presigner s3Presigner;

    @BeforeEach
    void setUp() throws IOException {
        String accessKey;
        String secretKey;
        String regionStr;

        // CI: 환경 변수 사용 (GitHub Secrets → AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, AWS_REGION, S3_BUCKET_NAME)
        if (System.getenv("AWS_ACCESS_KEY_ID") != null && System.getenv("AWS_SECRET_ACCESS_KEY") != null) {
            accessKey = System.getenv("AWS_ACCESS_KEY_ID").trim();
            secretKey = System.getenv("AWS_SECRET_ACCESS_KEY").trim();
            regionStr = (System.getenv("AWS_REGION") != null ? System.getenv("AWS_REGION") : System.getenv("AWS_DEFAULT_REGION") != null ? System.getenv("AWS_DEFAULT_REGION") : "ap-northeast-2").trim();
            this.bucketName = (System.getenv("S3_BUCKET_NAME") != null ? System.getenv("S3_BUCKET_NAME") : System.getenv("AWS_S3_BUCKET")).trim();
        } else if (new File(".env").exists()) {
            // 로컬: .env 파일 사용
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(".env")) {
                props.load(fis);
            }
            accessKey = props.getProperty("AWS_S3_ACCESS_KEY", "").trim();
            secretKey = props.getProperty("AWS_S3_SECRET_KEY", "").trim();
            regionStr = props.getProperty("AWS_S3_REGION", "ap-northeast-2").trim();
            this.bucketName = props.getProperty("AWS_S3_BUCKET", "").trim();
        } else {
            Assumptions.assumeTrue(false, "AWS credentials not found: set env vars or .env file");
            return;
        }

        Assumptions.assumeTrue(accessKey != null && !accessKey.isEmpty() && secretKey != null && !secretKey.isEmpty(),
                "AWS credentials are empty - skip S3 tests");

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
		@Order(1)
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
		@Order(2)
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
		@Order(3)
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
