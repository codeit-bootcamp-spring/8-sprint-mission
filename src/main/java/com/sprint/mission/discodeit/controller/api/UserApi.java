package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "User Controller", description = "사용자 API입니다.")
public interface UserApi {

  @Operation(summary = "사용자 생성")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "사용자 생성 성공", content = @Content(schema = @Schema(implementation = UserDto.class))),
      @ApiResponse(responseCode = "400", description = "중복된 정보를 가진 사용자 이미 존재해서 생성 불가", content = @Content(examples = @ExampleObject(value = "사용자가 이미 존재합니다."))),
  })
  ResponseEntity<UserDto> create(
      @Parameter(
          description = "사용자 생성 정보",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
      ) UserCreateRequest userCreateRequest,
      @Parameter(
          description = "사용자 프로필 이미지",
          content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
      ) MultipartFile profile
  );

  @Operation(summary = "사용자 정보 수정")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "사용자 정보 수정 성공", content = @Content(schema = @Schema(implementation = UserDto.class))),
      @ApiResponse(responseCode = "400", description = "중복된 정보를 가진 사용자 이미 존재해서 생성 불가", content = @Content(examples = @ExampleObject(value = "사용자가 이미 존재합니다."))),
      @ApiResponse(responseCode = "404", description = "사용자 조회 불가", content = @Content(examples = @ExampleObject(value = "{userId} 사용자를 찾을 수 없습니다.")))
  })
  ResponseEntity<UserDto> update(
      @Parameter(description = "수정할 사용자 Id") UUID userId,
      @Parameter(description = "수정할 사용자 정보") UserUpdateRequest userUpdateRequest,
      @Parameter(description = "수정할 사용자 프로필 이미지") MultipartFile profile
  );

  @Operation(summary = "사용자 정보 삭제")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "사용자 정보 삭제 성공"),
      @ApiResponse(responseCode = "404", description = "사용자 조회 불가", content = @Content(examples = @ExampleObject(value = "{userId} 사용자를 찾을 수 없습니다.")))
  })
  ResponseEntity<Void> delete(@Parameter(description = "삭제할 사용자 Id") UUID userId);

  @Operation(summary = "사용자 목록 수정")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "사용자 정보 수정 성공", content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserDto.class))))
  })
  ResponseEntity<List<UserDto>> findAll();
}
