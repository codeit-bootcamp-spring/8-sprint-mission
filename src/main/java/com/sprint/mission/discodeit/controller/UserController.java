package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;

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
  @GetMapping
  public ResponseEntity<List<UserDto>> findAll() {
    try {
      return ResponseEntity.ok(userService.findAll());
    } catch (Exception e) {
      log.error("사용자 목록 조회 중 오류 발생", e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * User 등록 POST /api/users (multipart/form-data)
   */
  @PostMapping(consumes = "multipart/form-data")
  public ResponseEntity<UserDto> createMultipart(
      @RequestPart("userCreateRequest") @Valid UserCreateRequest userCreateRequest,
      @RequestPart(value = "profile", required = false) MultipartFile profile) {

    Optional<BinaryContentCreateRequest> profileRequest = Optional.empty();
    if (profile != null && !profile.isEmpty()) {
      try {
        byte[] fileBytes = profile.getBytes();
        String contentType = profile.getContentType() != null ? profile.getContentType() : "image/png";
        profileRequest = Optional.of(new BinaryContentCreateRequest(
            profile.getOriginalFilename() != null ? profile.getOriginalFilename() : "profile",
            contentType,
            fileBytes
        ));
      } catch (Exception e) {
        throw new RuntimeException("프로필 이미지 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
      }
    }
    UserDto created = userService.create(userCreateRequest, profileRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  /**
   * User 등록 POST /api/users (application/json)
   */
  @PostMapping(consumes = "application/json")
  public ResponseEntity<UserDto> createJson(@RequestBody @Valid UserCreateRequest request) {
    UserDto created = userService.create(request, Optional.empty());
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  /**
   * User 정보 수정 PATCH /api/users/{userId} (multipart/form-data - 이미지 포함 가능)
   */
  @PatchMapping(value = "/{userId}", consumes = "multipart/form-data")
  public ResponseEntity<UserDto> updateMultipart(
      @PathVariable UUID userId,
      @RequestPart(value = "userUpdateRequest", required = false) @Valid UserUpdateRequest requestPart,
      @RequestPart(value = "profile", required = false) MultipartFile profile) {

    UserUpdateRequest actualRequest = requestPart != null
        ? requestPart
        : new UserUpdateRequest(null, null, null);

    Optional<BinaryContentCreateRequest> profileRequest = Optional.empty();
    if (profile != null && !profile.isEmpty()) {
      try {
        byte[] fileBytes = profile.getBytes();
        String contentType = profile.getContentType() != null ? profile.getContentType() : "image/png";
        profileRequest = Optional.of(new BinaryContentCreateRequest(
            profile.getOriginalFilename() != null ? profile.getOriginalFilename() : "profile",
            contentType,
            fileBytes
        ));
      } catch (Exception e) {
        throw new RuntimeException("프로필 이미지 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
      }
    }
    UserDto updated = userService.update(userId, actualRequest, profileRequest);
    return ResponseEntity.ok(updated);
  }

  /**
   * User 정보 수정 PATCH /api/users/{userId} (application/json - 이미지 없음)
   */
  @PatchMapping(value = "/{userId}", consumes = "application/json")
  public ResponseEntity<UserDto> updateJson(
      @PathVariable UUID userId,
      @RequestBody @Valid UserUpdateRequest requestBody) {
    UserDto updated = userService.update(userId, requestBody, Optional.empty());
    return ResponseEntity.ok(updated);
  }

  /**
   * User 삭제 DELETE /api/users/{userId}
   */
  @DeleteMapping("/{userId}")
  public ResponseEntity<Void> delete(@PathVariable UUID userId) {
    userService.delete(userId);
    return ResponseEntity.noContent().build();
  }
}
