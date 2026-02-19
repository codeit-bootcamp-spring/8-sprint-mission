package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.DTO.dto.UserDto;
import com.sprint.mission.discodeit.DTO.dto.UserStatusDto;
import com.sprint.mission.discodeit.DTO.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.DTO.request.UserCreateRequest;
import com.sprint.mission.discodeit.DTO.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.DTO.request.UserUpdateRequest;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;
  private final UserStatusService userStatusService;

  @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
  public ResponseEntity<UserDto> create(
      @Valid @RequestPart(value = "userCreateRequest") UserCreateRequest userCreateRequest,
      @RequestPart(value = "profile", required = false) MultipartFile profile) {
    log.info("Controller: 사용자 생성 요청 - email: {}, username: {}", userCreateRequest.email(),
        userCreateRequest.username());
    Optional<BinaryContentCreateRequest> profileRequest = Optional.ofNullable(profile)
        .flatMap(this::resolveProfileRequest);
    UserDto userDto = userService.create(userCreateRequest, profileRequest);
    log.info("Controller: 사용자 생성 완료 - ID: {}", userDto.id());
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(userDto);
  }

  @GetMapping
  public ResponseEntity<List<UserDto>> findAll() {
    List<UserDto> userResponses = userService.findAll();
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(userResponses);
  }

  @PatchMapping(
      path = "{userId}",
      consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}
  )
  public ResponseEntity<UserDto> update(
      @PathVariable(value = "userId") UUID userId,
      @Valid @RequestPart(value = "userUpdateRequest") UserUpdateRequest userUpdateRequest,
      @RequestPart(value = "profile", required = false) MultipartFile profile) {
    log.info("Controller: 사용자 정보 수정 요청 - ID: {}", userId);
    Optional<BinaryContentCreateRequest> profileRequest = Optional.ofNullable(profile)
        .flatMap(this::resolveProfileRequest);
    UserDto userDto = userService.update(userId, userUpdateRequest, profileRequest);
    log.info("Controller: 사용자 정보 수정 완료 - ID: {}", userDto.id());
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(userDto);
  }

  @PatchMapping(path = "{userId}/userStatus")
  public ResponseEntity<UserStatusDto> updateUserStatusByUserId(
      @PathVariable(value = "userId") UUID userId,
      @RequestBody UserStatusUpdateRequest userStatusUpdateRequest) {
    UserStatusDto updatedUserStatusDto = userStatusService.updateByUserId(userId,
        userStatusUpdateRequest);
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(updatedUserStatusDto);
  }

  @DeleteMapping(path = "/{userId}")
  public ResponseEntity<Void> delete(
      @PathVariable("userId") UUID userId) {
    log.info("Controller: 사용자 삭제 요청 - ID: {}", userId);
    userService.delete(userId);
    log.info("Controller: 사용자 삭제 완료 - ID: {}", userId);
    return ResponseEntity
        .status(HttpStatus.NO_CONTENT)
        .build();
  }

  private Optional<BinaryContentCreateRequest> resolveProfileRequest(MultipartFile profileFile) {
    if (profileFile.isEmpty()) {
      return Optional.empty();
    } else {
      try {
        BinaryContentCreateRequest binaryContentCreateRequest = new BinaryContentCreateRequest(
            profileFile.getOriginalFilename(),
            profileFile.getContentType(),
            profileFile.getBytes()
        );
        return Optional.of(binaryContentCreateRequest);
      } catch (IOException e) {
        log.error("Controller: 프로필 이미지 처리 중 오류 발생 - error: {}", e.getMessage());
        throw new RuntimeException(e);
      }
    }
  }
}
