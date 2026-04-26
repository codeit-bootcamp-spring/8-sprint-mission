package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.dto.data.NotificationDto;
import com.sprint.mission.discodeit.response.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

@Tag(name = "Notification", description = "알림 API")
public interface NotificationApi {

  @Operation(summary = "내 알림 목록 조회")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          content = @Content(array = @ArraySchema(schema = @Schema(implementation = NotificationDto.class)))
      ),
      @ApiResponse(
          responseCode = "401",
          description = "액세스 토큰 없음 또는 유효하지 않음",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  ResponseEntity<List<NotificationDto>> findAll(Authentication authentication);

  @Operation(summary = "알림 삭제 (본인 알림만)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "삭제됨"),
      @ApiResponse(
          responseCode = "401",
          description = "인증되지 않은 요청",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      ),
      @ApiResponse(
          responseCode = "403",
          description = "다른 사용자의 알림",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      ),
      @ApiResponse(
          responseCode = "404",
          description = "알림이 존재하지 않음",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  ResponseEntity<Void> delete(
      @Parameter(description = "알림 id") UUID notificationId,
      Authentication authentication
  );
}
