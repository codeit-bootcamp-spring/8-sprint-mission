package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusResponse;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;
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
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final UserStatusService userStatusService;

  // 유저 생성 (GET-only 미션 대응)
  @Operation(summary = "User 등록")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "User가 성공적으로 생성됨"),
      @ApiResponse(responseCode = "400",
          description = "같은 email 또는 username를 사용하는 User가 이미 존재함",
          content = @Content(
              mediaType = "*/*",
              examples = @ExampleObject(
                  value = "User with email {email} already exists"
              )
          )
      )
  })
  @PostMapping(
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE
  )
  public ResponseEntity<UserResponse> create(
      @RequestPart("userCreateRequest") UserCreateRequest userCreateRequest,

      @Parameter(
          description = "User 프로필 이미지",
          content = @Content(
              mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
              schema = @Schema(type = "string", format = "binary")
          )
      )
      @RequestPart(value = "profile", required = false) MultipartFile profile
  ) {
    // 파일이 존재할 경우만 -> DTO로 변환 -> 서비스 전달
    BinaryContentCreateRequest profileImgRequest = convertProfileInfo(profile);

    UserResponse response = userService.create(userCreateRequest, profileImgRequest);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(response);
  }

  // 유저 수정 (GET-only 미션 대응)
  @Operation(summary = "User 정보 수정")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "404", description = "User를 찾을 수 없음",
          content = @Content(
              mediaType = "*/*",
              examples = @ExampleObject(
                  value = "User with id {id} not found"
              )
          )),
      @ApiResponse(responseCode = "400",
          description = "같은 email 또는 username를 사용하는 User가 이미 존재함",
          content = @Content(
              mediaType = "*/*",
              examples = @ExampleObject(
                  value = "user with email {newEmail} already exists"
              )
          )
      ),
      @ApiResponse(responseCode = "200", description = "User 정보가 성공적으로 수정됨")
  })
  @PatchMapping(
      path = "{userId}",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE
  )
  public ResponseEntity<UserResponse> update(

      @Parameter(description = "수정할 User ID")
      @PathVariable UUID userId,

      @RequestPart("userUpdateRequest") UserUpdateRequest userUpdateRequest,

      @Parameter(description = "수정할 User 프로필 이미지")
      @RequestPart(value = "profile", required = false) MultipartFile profile
  ) {
    BinaryContentCreateRequest profileImgRequest = convertProfileInfo(profile);

    UserResponse response = userService.update(userId, userUpdateRequest, profileImgRequest);

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(response);
  }

  // 유저 삭제 (GET-only 미션 대응)
  @Operation(summary = "User 삭제")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "User가 성공적으로 삭제됨"),
      @ApiResponse(responseCode = "404", description = "User를 찾을 수 없음",
          content = @Content(
              mediaType = "*/*",
              examples = @ExampleObject(
                  value = "User with id {id} not found"
              )
          ))
  })
  @DeleteMapping("{userId}")
  public ResponseEntity<Void> delete(

      @Parameter(
          description = "삭제할 User ID",
          schema = @Schema(type = "string", format = "uuid")
      )
      @PathVariable UUID userId) {
    userService.delete(userId);
    return ResponseEntity
        .status(HttpStatus.NO_CONTENT)
        .build();
  }

  // 유저 다건 조회 (GET-only 미션 대응)
  @Operation(summary = "전체 User 목록 조회")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "User 목록 조회 성공")
  })
  @GetMapping
  public ResponseEntity<List<UserResponse>> findAll() {
    List<UserResponse> findList = userService.findAll();
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(findList);
  }

  // 사용자 온라인 상태 업데이트 (GET-only 미션 대응)
  @Operation(summary = "User 온라인 상태 업데이트")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "404", description = "해당 User의 UserStatus를 찾을 수 없음",
          content = @Content(
              mediaType = "*/*",
              examples = @ExampleObject(
                  value = "UserStatus with userId {userId} not found"
              )
          )),
      @ApiResponse(responseCode = "200", description = "User 온라인 상태가 성공적으로 업데이트됨")
  })
  @PatchMapping("{userId}/userStatus")
  public ResponseEntity<UserStatusResponse> updateUserStatusByUserId(

      @Parameter(
          description = "상태를 변경할 User ID",
          schema = @Schema(type = "string", format = "uuid")
      )
      @PathVariable UUID userId,
      @RequestBody UserStatusUpdateRequest request
  ) {
    UserStatusResponse updateUserStatus = userStatusService.updateByUserId(userId,
        request.newLastActiveAt());
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(updateUserStatus);
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
