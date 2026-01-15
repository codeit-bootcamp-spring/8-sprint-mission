package com.sprint.mission.discodeit.api;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusDto;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "User", description = "User API")
public interface UserApi {

  @Operation(summary = "User 등록")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "User가 성공적으로 생성됨"),
      @ApiResponse(responseCode = "400", description = "중복된 이메일 또는 유저네임 존재",
          content = @Content(mediaType = "*/*",
              examples = @ExampleObject(value = "User with email {email} already exists")))
  })
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  ResponseEntity<UserDto> create(
      @RequestPart("userCreateRequest") UserCreateRequest userCreateRequest,
      @Parameter(description = "User 프로필 이미지",
          content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
              schema = @Schema(type = "string", format = "binary")))
      @RequestPart(value = "profile", required = false) MultipartFile profile);

  @Operation(summary = "User 정보 수정")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "User 정보가 성공적으로 수정됨"),
      @ApiResponse(responseCode = "404", description = "User를 찾을 수 없음",
          content = @Content(mediaType = "*/*",
              examples = @ExampleObject(value = "User with id {id} not found"))),
      @ApiResponse(responseCode = "400", description = "중복된 데이터 존재")
  })
  @PatchMapping(path = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  ResponseEntity<UserDto> update(
      @Parameter(description = "수정할 User ID") @PathVariable UUID userId,
      @RequestPart("userUpdateRequest") UserUpdateRequest userUpdateRequest,
      @Parameter(description = "수정할 User 프로필 이미지")
      @RequestPart(value = "profile", required = false) MultipartFile profile);

  @Operation(summary = "User 삭제")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "User가 성공적으로 삭제됨"),
      @ApiResponse(responseCode = "404", description = "User를 찾을 수 없음")
  })
  @DeleteMapping("/{userId}")
  ResponseEntity<Void> delete(
      @Parameter(description = "삭제할 User ID", schema = @Schema(type = "string", format = "uuid"))
      @PathVariable UUID userId);

  @Operation(summary = "전체 User 목록 조회")
  @ApiResponse(responseCode = "200", description = "User 목록 조회 성공")
  @GetMapping
  ResponseEntity<List<UserDto>> findAll();

  @Operation(summary = "User 온라인 상태 업데이트")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "상태 업데이트 성공"),
      @ApiResponse(responseCode = "404", description = "UserStatus 찾을 수 없음")
  })
  @PatchMapping("/{userId}/userStatus")
  ResponseEntity<UserStatusDto> updateUserStatusByUserId(
      @Parameter(description = "상태를 변경할 User ID", schema = @Schema(type = "string", format = "uuid"))
      @PathVariable UUID userId,
      @RequestBody UserStatusUpdateRequest request);
}
