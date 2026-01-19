package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserResponse;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final BinaryContentService binaryContentService;

    @Override
    public UserResponse create(UserCreateRequest request) {
        // 1. 중복 검사
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        // 2. 프로필 이미지 처리
        UUID profileId;
        
        // 프로필 이미지가 제공된 경우 BinaryContent로 저장
        if (request.getProfileImage() != null && !request.getProfileImage().isEmpty()) {
            BinaryContentCreateRequest binaryRequest = new BinaryContentCreateRequest(
                    request.getFileName() != null ? request.getFileName() : "profile.png",
                    request.getFileType() != null ? request.getFileType() : "image/png",
                    request.getFileSize() != null ? request.getFileSize() : 0L,
                    request.getProfileImage()
            );
            BinaryContent savedContent = binaryContentService.create(binaryRequest);
            profileId = savedContent.getId();
        } else {
            // 프로필 이미지가 없는 경우 기본 BinaryContent 생성 (나중에 DataInitializer에서 할당)
            BinaryContent profileContent = new BinaryContent();
            BinaryContent savedContent = binaryContentRepository.save(profileContent);
            profileId = savedContent.getId();
        }

        // 3. User 객체 생성 및 저장 (반드시 저장된 객체를 변수에 담으세요)
        User user = new User(
                request.getUsername() != null ? request.getUsername() : request.getName(),
                request.getEmail(),
                request.getPassword(),
                profileId
        );
        User savedUser = userRepository.save(user); //  중요: 리포지토리가 반환하는 객체 사용

        // 4. 상태 저장
        userStatusRepository.save(new UserStatus(savedUser.getId()));

        // 5. 프로필 이미지가 없는 경우 이름에 맞는 이미지 할당
        if (request.getProfileImage() == null || request.getProfileImage().isEmpty()) {
            try {
                // DataInitializer의 로직을 재사용하여 이름에 맞는 프로필 이미지 할당
                assignProfileImageToNewUser(savedUser.getId(), savedUser.getName());
            } catch (Exception e) {
                // 프로필 이미지 할당 실패는 치명적이지 않으므로 로그만 출력
                System.err.println("프로필 이미지 자동 할당 실패: " + e.getMessage());
            }
        }

        // 6. 저장된 'savedUser'를 DTO로 변환
        return convertToResponse(savedUser);
    }
    
    /**
     * 새 사용자에게 이름에 맞는 프로필 이미지 할당
     */
    private void assignProfileImageToNewUser(UUID userId, String userName) {
        // 이름에 맞는 이미지 파일 선택
        String imageFileName = getImageFileNameForUser(userName);
        
        try {
            // static/images 폴더에서 이미지 파일 읽기
            org.springframework.core.io.ClassPathResource resource = 
                    new org.springframework.core.io.ClassPathResource("static/images/" + imageFileName);
            
            if (!resource.exists()) {
                resource = new org.springframework.core.io.ClassPathResource("static/images/default-avatar.png");
            }
            
            byte[] imageBytes = resource.getInputStream().readAllBytes();
            String base64Bytes = java.util.Base64.getEncoder().encodeToString(imageBytes);
            
            // BinaryContent 생성
            BinaryContentCreateRequest binaryRequest = new BinaryContentCreateRequest(
                    imageFileName,
                    "image/png",
                    (long) imageBytes.length,
                    base64Bytes
            );
            
            BinaryContent binaryContent = binaryContentService.create(binaryRequest);
            
            // 사용자 프로필 이미지 업데이트
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            user.updateProfileId(binaryContent.getId());
            userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("프로필 이미지 할당 실패: " + e.getMessage(), e);
        }
    }
    
    private String getImageFileNameForUser(String userName) {
        if (userName == null) {
            return "default-avatar.png";
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
        
        // 매칭되지 않으면 기본 이미지
        return "default-avatar.png";
    }

    /**
     * 유저 수정: 선택적 프로필 이미지 교체 및 이름 중복 검사
     */
    @Override
    public UserResponse update(UserUpdateRequest request) {
        // 1. 사용자 조회
        User user = userRepository.findById(request.getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 2. 이름 변경 (name이 제공된 경우)
        String newName = request.getName();
        String newPassword = request.getPassword();
        
        // 이름이 변경되는 경우에만 중복 검사
        if (newName != null && !newName.equals(user.getName())) {
            if (userRepository.existsByName(newName)) {
                throw new IllegalArgumentException("이미 사용 중인 이름입니다.");
            }
        }

        // 3. 업데이트할 필드들을 한 번에 처리
        user.update(newName, null, newPassword);

        // 4. 프로필 이미지 ID 업데이트 (profileId가 제공된 경우)
        if (request.getProfileId() != null) {
            user.updateProfileId(request.getProfileId());
        }

        // 5. 사용자 저장
        User savedUser = userRepository.save(user);

        // 6. 응답 DTO 변환
        return convertToResponse(savedUser);
    }

    @Override
    public UserResponse create(String name, String email, String password, String profileImage) {
        return null;
    }

    @Override
    public UserResponse login(String email, String password) {
        return null;
    }

    @Override
    public List<UserResponse> findAll() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse findById(UUID id) {
        return null;
    }

    @Override
    public UserResponse update(UUID id, String name, String password, String profileImage) {
        return null;
    }

    // ... findAll, findById 등 생략

    @Override
    public void delete(UUID id) {
        // UserStatus 조회 후 삭제
        userStatusRepository.findByUserId(id)
                .ifPresent(status -> userStatusRepository.delete(status.getId()));
        userRepository.delete(id);
    }

    private UserResponse convertToResponse(User user) {
        UserStatus status = userStatusRepository.findByUserId(user.getId())
                .orElse(new UserStatus(user.getId()));

        //  User 엔티티의 Getter를 통해 값을 DTO로 복사
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                status.getId(),
                user.getId(),
                status.isOnline(),
                user.getProfileId()
        );
    }

    // 이 외 메서드들도 convertToResponse(savedUser) 형태로 반환하도록 확인하세요.
}