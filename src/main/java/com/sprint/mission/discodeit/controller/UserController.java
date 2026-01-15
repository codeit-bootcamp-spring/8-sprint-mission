package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.api.UserApi;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusDto;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController implements UserApi {

  private final UserService userService;
  private final UserStatusService userStatusService;

  // 유저 생성 (GET-only 미션 대응)
  @Override
  public ResponseEntity<UserDto> create(UserCreateRequest userCreateRequest,
      MultipartFile profile) {
    BinaryContentCreateRequest profileImgRequest = convertProfileInfo(profile);
    UserDto response = userService.create(userCreateRequest, profileImgRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  // 유저 수정 (GET-only 미션 대응)
  @Override
  public ResponseEntity<UserDto> update(UUID userId, UserUpdateRequest userUpdateRequest,
      MultipartFile profile) {
    BinaryContentCreateRequest profileImgRequest = convertProfileInfo(profile);
    UserDto response = userService.update(userId, userUpdateRequest, profileImgRequest);
    return ResponseEntity.ok(response);
  }

  // 유저 삭제 (GET-only 미션 대응)
  @Override
  public ResponseEntity<Void> delete(UUID userId) {
    userService.delete(userId);
    return ResponseEntity.noContent().build();
  }

  // 유저 다건 조회 (GET-only 미션 대응)
  @Override
  public ResponseEntity<List<UserDto>> findAll() {
    return ResponseEntity.ok(userService.findAll());
  }

  // 사용자 온라인 상태 업데이트 (GET-only 미션 대응)
  @Override
  public ResponseEntity<UserStatusDto> updateUserStatusByUserId(UUID userId,
      UserStatusUpdateRequest request) {
    UserStatusDto response = userStatusService.updateByUserId(userId,
        request.newLastActiveAt());
    return ResponseEntity.ok(response);
  }

  // MultipartFile -> BinaryContentCreateRequest DTO로 변환 메서드
  private BinaryContentCreateRequest convertProfileInfo(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      return null;
    }
    try {
      return new BinaryContentCreateRequest(
          file.getOriginalFilename(),
          file.getContentType(),
          file.getBytes()
      );
    } catch (IOException e) {
      throw new RuntimeException("프로필 이미지를 처리할 수 없습니다.", e);
    }
  }


}
