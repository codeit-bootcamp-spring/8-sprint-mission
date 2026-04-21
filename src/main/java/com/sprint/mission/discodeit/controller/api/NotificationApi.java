package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.dto.data.NotificationDto;
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

@Tag(name = "Notification", description = "알림 API")
public interface NotificationApi {

  @Operation(summary = "내 알림 목록 조회")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200", description = "알림 목록 조회 성공",
          content = @Content(array = @ArraySchema(schema = @Schema(implementation = NotificationDto.class)))
      )
  })
  ResponseEntity<List<NotificationDto>> findAllByReceiverId(
      @Parameter(description = "수신자(내) ID") UUID receiverId
  );

  @Operation(summary = "알림 확인 (삭제)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "알림 확인 완료"),
      @ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음"),
      @ApiResponse(responseCode = "403", description = "접근 권한 없음")
  })
  ResponseEntity<Void> delete(
      @Parameter(description = "삭제할 알림 ID") UUID notificationId,
      @Parameter(description = "요청자 ID") UUID requesterId
  );
}
