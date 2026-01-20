package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserResponse;
import com.sprint.mission.discodeit.dto.UserStatusRequest;
import com.sprint.mission.discodeit.dto.UserStatusResponse;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final UserStatusService userStatusService;
  private final BinaryContentService binaryContentService;

  /**
   * 모든 사용자 조회 GET /api/users
   */
  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<List<UserDto>> findAll() {
    try {
      List<UserDto> userDtos = userService.findAll().stream()
          .map(response -> {
            // profileId로 BinaryContent 조회하여 BinaryContentDto 생성
            com.sprint.mission.discodeit.dto.BinaryContentDto profileDto = null;
            if (response.getProfileId() != null) {
              binaryContentService.findById(response.getProfileId())
                  .ifPresent(binaryContent -> {
                    // BinaryContentDto는 별도로 생성해야 함
                  });
              // 간단히 변환
              var binaryContent = binaryContentService.findById(response.getProfileId()).orElse(null);
              if (binaryContent != null) {
                profileDto = new com.sprint.mission.discodeit.dto.BinaryContentDto(
                    binaryContent.getId(),
                    binaryContent.getFileName(),
                    binaryContent.getFileSize(),
                    binaryContent.getContentType()
                );
              }
            }
            
            return UserDto.builder()
                .id(response.getId())
                .username(response.getName() != null ? response.getName() : "Unknown")
                .email(response.getEmail() != null ? response.getEmail() : "")
                .profile(profileDto)
                .online(response.isOnline())
                .build();
          })
          .collect(Collectors.toList());

      return ResponseEntity.ok(userDtos);
    } catch (Exception e) {
      //  printStackTrace() 대신 로깅 사용
      log.error("사용자 목록 조회 중 오류 발생", e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * User 등록 POST /api/users (multipart/form-data)
   */
  @RequestMapping(method = RequestMethod.POST, consumes = "multipart/form-data")
  public ResponseEntity<UserResponse> createMultipart(
      @RequestPart("userCreateRequest") UserCreateRequest userCreateRequest,
      @RequestPart(value = "profile", required = false) MultipartFile profile) {

    // 프로필 이미지 파일이 있는 경우 처리
    if (profile != null && !profile.isEmpty()) {
      try {
        byte[] fileBytes = profile.getBytes();
        String base64Bytes = java.util.Base64.getEncoder().encodeToString(fileBytes);

        // UserCreateRequest에 프로필 이미지 정보 설정
        userCreateRequest.setFileName(profile.getOriginalFilename());
        userCreateRequest.setFileType(
            profile.getContentType() != null ? profile.getContentType() : "image/png");
        userCreateRequest.setFileSize(profile.getSize());
        userCreateRequest.setProfileImage(base64Bytes);
      } catch (Exception e) {
        throw new RuntimeException("프로필 이미지 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
      }
    }

    return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
        .body(userService.create(userCreateRequest));
  }

  /**
   * User 등록 POST /api/users (application/json)
   */
  @RequestMapping(method = RequestMethod.POST, consumes = "application/json")
  public ResponseEntity<UserResponse> createJson(@RequestBody UserCreateRequest request) {
    return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
        .body(userService.create(request));
  }

  /**
   * User 정보 수정 PATCH /api/users/{userId} (multipart/form-data - 이미지 포함 가능)
   */
  @RequestMapping(value = "/{userId}", method = RequestMethod.PATCH, consumes = "multipart/form-data")
  public ResponseEntity<UserResponse> updateMultipart(
      @PathVariable UUID userId,
      @RequestPart(value = "userUpdateRequest", required = false) UserUpdateRequest requestPart,
      @RequestPart(value = "profile", required = false) MultipartFile profile) {

    // userUpdateRequest가 없으면 기본 객체 생성
    UserUpdateRequest actualRequest;
    if (requestPart != null) {
      actualRequest = requestPart;
      actualRequest.setId(userId);
    } else {
      actualRequest = new UserUpdateRequest();
      actualRequest.setId(userId);
    }

    UUID profileId = actualRequest.getProfileId();

    // 프로필 이미지 파일이 있는 경우 BinaryContent로 저장하고 profileId 설정
    if (profile != null && !profile.isEmpty()) {
      try {
        byte[] fileBytes = profile.getBytes();
        String base64Bytes = java.util.Base64.getEncoder().encodeToString(fileBytes);

        BinaryContentCreateRequest binaryRequest = new BinaryContentCreateRequest(
            profile.getOriginalFilename(),
            profile.getContentType() != null ? profile.getContentType() : "image/png",
            profile.getSize(),
            base64Bytes
        );
        var binaryContent = binaryContentService.create(binaryRequest);
        profileId = binaryContent.getId();
      } catch (Exception e) {
        throw new RuntimeException("프로필 이미지 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
      }
    }

    // 경로 파라미터의 userId를 사용하여 UserUpdateRequest 생성
    UserUpdateRequest updateRequest = new UserUpdateRequest();
    updateRequest.setId(userId);
    updateRequest.setNewUsername(actualRequest.getNewUsername() != null ? actualRequest.getNewUsername() : actualRequest.getName());
    updateRequest.setNewEmail(actualRequest.getNewEmail());
    updateRequest.setNewPassword(actualRequest.getNewPassword() != null ? actualRequest.getNewPassword() : actualRequest.getPassword());
    updateRequest.setProfileImage(actualRequest.getProfileImage());
    updateRequest.setProfileId(profileId != null ? profileId : actualRequest.getProfileId());
    
    return ResponseEntity.ok(userService.update(updateRequest));
  }

  /**
   * User 정보 수정 PATCH /api/users/{userId} (application/json - 이미지 없음)
   */
  @RequestMapping(value = "/{userId}", method = RequestMethod.PATCH, consumes = "application/json")
  public ResponseEntity<UserResponse> updateJson(
      @PathVariable UUID userId,
      @RequestBody UserUpdateRequest requestBody) {

    // 경로 파라미터의 userId를 사용하여 UserUpdateRequest 생성
    UserUpdateRequest updateRequest = new UserUpdateRequest();
    updateRequest.setId(userId);
    updateRequest.setNewUsername(requestBody.getNewUsername() != null ? requestBody.getNewUsername() : requestBody.getName());
    updateRequest.setNewEmail(requestBody.getNewEmail());
    updateRequest.setNewPassword(requestBody.getNewPassword() != null ? requestBody.getNewPassword() : requestBody.getPassword());
    updateRequest.setProfileImage(requestBody.getProfileImage());
    updateRequest.setProfileId(requestBody.getProfileId());
    
    return ResponseEntity.ok(userService.update(updateRequest));
  }

  /**
   * User 삭제 DELETE /api/users/{userId}
   */
  @RequestMapping(value = "/{userId}", method = RequestMethod.DELETE)
  public ResponseEntity<Void> delete(@PathVariable UUID userId) {
    userService.delete(userId);
    return ResponseEntity.noContent().build();
  }

  /**
   * User 온라인 상태 업데이트 PATCH /api/users/{userId}/userStatus
   * <p>
   * [활용 방법 및 호출 시점] 1. 클라이언트 앱 실행 시: 앱이 백그라운드에서 포그라운드로 전환될 때 호출하여 사용자를 온라인 상태로 만듭니다. 2. 주기적 하트비트:
   * 앱이 활성화된 동안 일정 간격(예: 1분)마다 호출하여 사용자의 온라인 상태를 유지합니다. 3. 앱 종료 시: 앱이 완전히 종료되기 전 호출할 수 있으나, 서버 측에서
   * 일정 시간(예: 5분) 동안 업데이트가 없으면 자동으로 오프라인으로 처리하는 것이 더 안전합니다.
   * <p>
   * [클라이언트 앱에서의 반영] 1. 다른 사용자의 목록 화면: 해당 사용자의 온라인/오프라인 상태 배지가 실시간으로 업데이트됩니다. (예: "사용자 목록" 화면에서 초록색
   * "온라인" 배지 표시) 2. 채팅 목록: 각 채널에서 상대방의 온라인 상태가 표시되어 현재 대화 가능 여부를 알 수 있습니다. 3. 실시간 상태 동기화: 클라이언트 앱은
   * 주기적으로 사용자 목록 API를 호출하거나 WebSocket을 통해 상태 변경을 받아 화면에 반영합니다.
   * <p>
   * [기술적 고려사항] - lastAccessAt 필드가 현재 시간으로 업데이트됩니다. - 서버에서 isOnline() 메서드는 lastAccessAt 기준으로
   * 5분(300초) 이내 접근 시 온라인으로 판단합니다. - 이를 통해 네트워크 오류나 앱 크래시로 인한 상태 업데이트 실패를 자동으로 처리할 수 있습니다.
   */
  @RequestMapping(value = "/{userId}/userStatus", method = RequestMethod.PATCH)
  public ResponseEntity<UserStatusResponse> updateStatus(@PathVariable UUID userId,
      @RequestBody UserStatusRequest request) {
    // 경로 파라미터의 userId를 사용하여 UserStatusRequest 생성
    UserStatusRequest statusRequest = new UserStatusRequest(userId);
    UserStatusResponse response = userStatusService.update(statusRequest);
    return ResponseEntity.ok(response);
  }
}