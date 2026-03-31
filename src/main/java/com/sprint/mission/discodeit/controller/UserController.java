package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.api.UserApi;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.service.UserService;
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

  // 유저 생성
  @Override
  public ResponseEntity<UserDto> create(UserCreateRequest userCreateRequest,
      MultipartFile profile) {
    BinaryContentCreateRequest binaryRequest = toBinaryRequest(profile);

    UserDto createdUser = userService.create(userCreateRequest, binaryRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
  }

  // 유저 수정
  @Override
  public ResponseEntity<UserDto> update(UUID userId, UserUpdateRequest userUpdateRequest,
      MultipartFile profile) {
    BinaryContentCreateRequest binaryRequest = toBinaryRequest(profile);

    UserDto updatedUser = userService.update(userId, userUpdateRequest, binaryRequest);
    return ResponseEntity.ok(updatedUser);
  }

  // 유저 삭제
  @Override
  public ResponseEntity<Void> delete(UUID userId) {
    userService.delete(userId);
    return ResponseEntity.noContent().build();
  }

  // 유저 다건 조회
  @Override
  public ResponseEntity<List<UserDto>> findAll() {
    return ResponseEntity.ok(userService.findAll());
  }

  private BinaryContentCreateRequest toBinaryRequest(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      return null;
    }
    try {
      return new BinaryContentCreateRequest(
          file.getOriginalFilename(),
          file.getSize(),
          file.getContentType(),
          file.getBytes()
      );
    } catch (IOException e) {
      throw new IllegalArgumentException("프로필 이미지를 처리할 수 없습니다.", e);
    }
  }


}
