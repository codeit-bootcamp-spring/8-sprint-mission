package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserStatusRequest;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Base64;
import java.util.List;
import java.util.Random;

// TODO: 개발 단계에서 데이터 생성 결과를 확인하기 위한 임시 컴포넌트입니다.
// 개발 완료 후 삭제 가능합니다.
@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final UserService userService;
    private final UserStatusService userStatusService;
    private final BinaryContentService binaryContentService;
    private final Random random = new Random();

    private static final String[] IMAGE_FILES = {
            "aliens.png", "buzz.png", "hamm.png", "jessie.png",
            "mrpotato.png", "mrspotato.png", "rex.png", "woody.png"
    };

    @PostConstruct
    public void init() {
        boolean isNewUsers = userService.findAll().isEmpty();

        if (isNewUsers) {
            // 초기 사용자 데이터 생성
            String[] usernames = {"jessie", "rex", "buzz", "woody"};
            String[] emails = {"jessie@codeit.com", "rex@codeit.com", "buzz@codeit.com", "woody@codeit.com"};

            for (int i = 0; i < usernames.length; i++) {
                // UserService를 사용하여 사용자 생성 (UserStatus도 자동 생성됨)
                UserCreateRequest createRequest = new UserCreateRequest(
                        usernames[i],
                        emails[i],
                        "password", // 기본 비밀번호
                        null, // fileName
                        null, // fileType
                        null, // fileSize
                        null  // profileImage
                );
                var userResponse = userService.create(createRequest);

                // UserStatusService를 사용하여 온라인 상태로 설정
                UserStatusRequest statusRequest = new UserStatusRequest(userResponse.getId());
                userStatusService.update(statusRequest);
            }
        }

        // 모든 사용자에게 랜덤 프로필 이미지 배정
        assignRandomProfileImages();
    }

    private void assignRandomProfileImages() {
        List<com.sprint.mission.discodeit.dto.UserResponse> users = userService.findAll();
        
        for (var user : users) {
            try {
                // 사용자 이름에 맞는 이미지 파일 선택
                String imageFileName = getImageFileNameForUser(user.getName());
                
                // 이미지 파일 읽기
                ClassPathResource resource = new ClassPathResource("static/images/" + imageFileName);
                byte[] imageBytes;
                try (InputStream inputStream = resource.getInputStream()) {
                    imageBytes = inputStream.readAllBytes();
                }
                
                // BinaryContent 생성
                BinaryContentCreateRequest binaryRequest = new BinaryContentCreateRequest(
                        imageFileName,
                        "image/png",
                        (long) imageBytes.length,
                        Base64.getEncoder().encodeToString(imageBytes)
                );
                
                var binaryContent = binaryContentService.create(binaryRequest);
                
                // 사용자 프로필 이미지 업데이트
                UserUpdateRequest updateRequest = new UserUpdateRequest(
                        user.getId(),
                        null, // name 변경 없음
                        null, // password 변경 없음
                        null, // profileImage (사용 안 함)
                        binaryContent.getId() // profileId 설정
                );
                
                userService.update(updateRequest);
            } catch (Exception e) {
                System.err.println("프로필 이미지 배정 실패 (사용자 ID: " + user.getId() + ", 이름: " + user.getName() + "): " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private String getImageFileNameForUser(String userName) {
        if (userName == null) {
            return IMAGE_FILES[random.nextInt(IMAGE_FILES.length)];
        }
        
        String lowerName = userName.toLowerCase();
        
        // 이름에 맞는 이미지 파일 매핑
        if (lowerName.contains("buzz")) {
            return "buzz.png";
        } else if (lowerName.contains("jessie") || lowerName.contains("제시")) {
            return "jessie.png";
        } else if (lowerName.contains("rex") || lowerName.contains("렉스")) {
            return "rex.png";
        } else if (lowerName.contains("woody") || lowerName.contains("우디")) {
            return "woody.png";
        } else if (lowerName.contains("현승원")) {
            return "aliens.png";
        }
        
        // 매칭되지 않으면 랜덤 선택
        return IMAGE_FILES[random.nextInt(IMAGE_FILES.length)];
    }
}

