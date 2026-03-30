package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.UserApi;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.service.UserService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController implements UserApi {

  private final UserService userService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UserDto> create(
      @Valid @RequestPart("userCreateRequest") UserCreateRequest userCreateRequest,
      @RequestPart(value = "profile", required = false) MultipartFile profile
  ) {
    log.info("[UserController] 사용자 생성 요청 - 이름: {}", userCreateRequest.username());

    Optional<BinaryContentCreateRequest> profileCreateRequest = toBinaryContentRequest(profile);

    UserDto user = userService.create(userCreateRequest, profileCreateRequest);

    log.info("[UserController] 사용자 생성 완료 - Id: {}", user.id());
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(user);
  }

  @PatchMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UserDto> update(
      @PathVariable UUID userId,
      @Valid @RequestPart("userUpdateRequest") UserUpdateRequest userUpdateRequest,
      @RequestPart(value = "profile", required = false) MultipartFile profile
  ) {
    log.info("[UserController] 사용자 수정 요청 - Id: {}", userId);

    Optional<BinaryContentCreateRequest> profileUpdateRequest = toBinaryContentRequest(profile);

    UserDto user = userService.update(userId, userUpdateRequest, profileUpdateRequest);

    log.info("[UserController] 사용자 수정 완료 - Id: {}", user.id());
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(user);
  }

  @DeleteMapping(value = "/{userId}")
  public ResponseEntity<Void> delete(@PathVariable UUID userId) {
    log.info("[UserController] 사용자 삭제 요청 - Id: {}", userId);

    userService.delete(userId);

    log.info("[UserController] 사용자 삭제 완료 - Id: {}", userId);
    return ResponseEntity
        .status(HttpStatus.NO_CONTENT)
        .build(); // 응답 바디가 없지만 ResponseEntity를 만들 때 build() 사용
  }

  @GetMapping
  public ResponseEntity<List<UserDto>> findAll() {
    List<UserDto> userDTOList = userService.findAll();

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(userDTOList);
  }

  Optional<BinaryContentCreateRequest> toBinaryContentRequest(MultipartFile profile) {
    return Optional.ofNullable(profile)
        .filter(p -> !p.isEmpty())
        .map(BinaryContentCreateRequest::fileFromRequest);
  }
}
