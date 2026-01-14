package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.DTO.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.DTO.request.UserCreateRequest;
import com.sprint.mission.discodeit.DTO.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.DTO.request.UserUpdateRequest;
import com.sprint.mission.discodeit.DTO.response.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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

@Tag(name = "User", description = "User API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;
  private final UserStatusService userStatusService;

  @Operation(summary = "User 등록", operationId = "create")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "201",
          description = "User가 성공적으로 생성됨",
          content = @Content(
              schema = @Schema(implementation = User.class)
          )
      ),
      @ApiResponse(
          responseCode = "400",
          description = "같은 email 또는 username를 사용하는 User가 이미 존재함",
          content = @Content(
              examples = @ExampleObject(value = "User with email {email} already exists")
          )
      )
  })
  @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
  public ResponseEntity<User> create(
      @Parameter(description = "User 생성 요청 정보", required = true)
      @RequestPart(value = "userCreateRequest") UserCreateRequest userCreateRequest,
      @Parameter(description = "User 프로필 이미지")
      @RequestPart(value = "profile", required = false) MultipartFile profile) {
    Optional<BinaryContentCreateRequest> profileRequest = Optional.ofNullable(profile)
        .flatMap(this::resolveProfileRequest);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(userService.create(userCreateRequest, profileRequest));
  }

  @Operation(summary = "전체 User 목록 조회", operationId = "findAll")
  @ApiResponse(
      responseCode = "200",
      description = "User 목록 조회 성공",
      content = @Content(
          schema = @Schema(implementation = UserDto.class)
      )
  )
  @GetMapping
  public ResponseEntity<List<UserDto>> findAll() {
    List<UserDto> userResponses = userService.findAll();
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(userResponses);
  }

  @Operation(summary = "User 정보 수정", operationId = "update")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "404",
          content = @Content(
              examples = @ExampleObject(value = "User with id {userId} not found")
          )
      ),
      @ApiResponse(
          responseCode = "400",
          description = "같은 email 또는 username를 사용하는 User가 이미 존재함",
          content = @Content(
              examples = @ExampleObject(value = "User with email {newEmail} already exists")
          )
      ),
      @ApiResponse(
          responseCode = "200",
          description = "User 정보가 성공적으로 수정됨",
          content = @Content(
              schema = @Schema(implementation = User.class)
          )
      )
  })
  @PatchMapping(
      path = "{userId}",
      consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}
  )
  public ResponseEntity<User> update(
      @Parameter(description = "수정할 UserID", required = true)
      @PathVariable(value = "userId") UUID userId,
      @Parameter(description = "User 변경 요청 정보", required = true)
      @RequestPart(value = "userUpdateRequest") UserUpdateRequest userUpdateRequest,
      @Parameter(description = "수정할 User 프로필 이미지")
      @RequestPart(value = "profile", required = false) MultipartFile profile) {
    Optional<BinaryContentCreateRequest> profileRequest = Optional.ofNullable(profile)
        .flatMap(this::resolveProfileRequest);
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(userService.update(userId, userUpdateRequest, profileRequest));
  }

  @Operation(summary = "User 온라인 상태 업데이트", operationId = "updateUserStatusByUserId")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "404",
          description = "해당 User의 UserStatus를 찾을 수 없음",
          content = @Content(
              examples = @ExampleObject(value = "UserStatus with userId {userId} not found")
          )
      ),
      @ApiResponse(
          responseCode = "200",
          description = "User 온라인 상태가 성공적으로 업데이트됨",
          content = @Content(
              schema = @Schema(implementation = UserStatus.class)
          )
      )
  })
  @PatchMapping(path = "{userId}/userStatus")
  public ResponseEntity<UserStatus> updateUserStateByUserId(
      @Parameter(description = "상태를 변경할 User ID", required = true)
      @PathVariable(value = "userId") UUID userId,
      @Parameter(description = "유저 온라인 상태 업데이트 요청", required = true)
      @RequestBody UserStatusUpdateRequest userStatusUpdateRequest) {
    UserStatus updatedUserStatus = userStatusService.updateByUserId(userId,
        userStatusUpdateRequest);
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(updatedUserStatus);
  }

  @Operation(summary = "User 삭제", operationId = "delete")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "204",
          description = "User가 성공적으로 삭제됨"
      ),
      @ApiResponse(
          responseCode = "404",
          description = "User를 찾을 수 없음",
          content = @Content(
              examples = @ExampleObject(value = "User with id {id} not found")
          )
      )
  })
  @DeleteMapping(path = "/{userId}")
  public ResponseEntity<Void> delete(
      @Parameter(description = "삭제할 User ID")
      @PathVariable("userId") UUID userId) {
    userService.delete(userId);
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
        throw new RuntimeException(e);
      }
    }
  }
}
