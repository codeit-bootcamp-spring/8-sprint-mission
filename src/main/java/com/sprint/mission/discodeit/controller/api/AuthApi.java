package com.sprint.mission.discodeit.controller.api;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Auth Controller", description = "사용자 인증 API입니다.")
public interface AuthApi {

  /*@Operation(summary = "로그인")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "사용자 정보 일치", content = @Content(schema = @Schema(implementation = UserDto.class))),
      @ApiResponse(responseCode = "400", description = "비밀번호 불일치", content = @Content(examples = @ExampleObject(value = "비밀번호가 일치하지 않습니다."))),
      @ApiResponse(responseCode = "404", description = "사용자 조회 불가", content = @Content(examples = @ExampleObject(value = "{username} 사용자를 찾을 수 없습니다.")))
  })
  ResponseEntity<UserDto> login(@Parameter(description = "로그인 정보") LoginRequest loginRequest);*/
}
