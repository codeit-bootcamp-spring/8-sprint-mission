package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.RoleUpdateRequest;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Auth", description = "인증 API")
public interface AuthApi {

  @Operation(summary = "현재 사용자 정보 조회")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "조회 성공",
          content = @Content(schema = @Schema(implementation = UserDto.class))),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
  })
  ResponseEntity<UserDto> me(@Parameter(hidden = true) DiscodeitUserDetails userDetails);

  @Operation(summary = "사용자 권한 수정", description = "관리자가 특정 사용자의 권한을 변경합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "권한 수정 성공",
          content = @Content(schema = @Schema(implementation = UserDto.class))),
      @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
  })
  ResponseEntity<UserDto> updateRole(@RequestBody RoleUpdateRequest request);
}
