package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.DTO.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.DTO.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.DTO.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.DTO.response.ChannelResponse;
import com.sprint.mission.discodeit.service.ChannelService;
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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Channel", description = "Channel API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/channels")
public class ChannelController {

  private final ChannelService channelService;

  @Operation(summary = "Public Channel 생성", operationId = "create_3")
  @ApiResponse(
      responseCode = "201",
      description = "Public Channel이 성공적으로 생성됨",
      content = @Content(
          schema = @Schema(
              implementation = PublicChannelCreateRequest.class
          )
      )
  )
  @PostMapping("public")
  public ResponseEntity<ChannelResponse> createPublicChannel(
      @RequestBody PublicChannelCreateRequest channelCreateRequest) {
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(channelService.create(channelCreateRequest));
  }

  @Operation(summary = "Private Channel 생성", operationId = "create_4")
  @ApiResponse(
      responseCode = "201",
      description = "Private Channel이 성공적으로 생성됨",
      content = @Content(
          schema = @Schema(
              implementation = PublicChannelCreateRequest.class
          )
      )
  )
  @PostMapping("private")
  public ResponseEntity<ChannelResponse> createPrivateChannel(
      @RequestBody PrivateChannelCreateRequest channelCreateRequest) {
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(channelService.create(channelCreateRequest));
  }

  @Operation(summary = "User가 참여 중인 Channel 목록 조회", operationId = "findAll_1")
  @ApiResponse(
      responseCode = "200",
      description = "Channel 목록 조회 성공",
      content = @Content(
          schema = @Schema(implementation = ChannelResponse.class)
      )
  )
  @GetMapping
  public ResponseEntity<List<ChannelResponse>> findAll(
      @Parameter(description = "조회할 User ID", required = true)
      @RequestParam(value = "userId") UUID userId) {
    List<ChannelResponse> channelResponse = channelService.findAll(userId);
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(channelResponse);
  }

  @Operation(summary = "Channel 정보 수정", operationId = "update_3")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "404",
          description = "Channel을 찾을 수 없음",
          content = @Content(
              examples = @ExampleObject(value = "Channel with id {channelId} not found")
          )
      ),
      @ApiResponse(
          responseCode = "400",
          description = "Private Channel은 수정할 수 없음",
          content = @Content(
              examples = @ExampleObject(value = "Private channel cannot be updated")
          )
      ),
      @ApiResponse(
          responseCode = "200",
          description = "Channel 정보가 성공적으로 수정됨",
          content = @Content(
              schema = @Schema(
                  implementation = ChannelResponse.class
              )
          )
      )
  })
  @PatchMapping("{channelId}")
  public ResponseEntity<ChannelResponse> update(
      @Parameter(description = "수정할 Channel ID", required = true)
      @PathVariable("channelId") UUID channelId,
      @RequestBody PublicChannelUpdateRequest channelUpdateRequest) {
    ChannelResponse channelResponse = channelService.update(channelId, channelUpdateRequest);
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(channelResponse);
  }

  @Operation(summary = "Channel 삭제", operationId = "delete_2")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "404",
          description = "Channel을 찾을 수 없음",
          content = @Content(
              examples = @ExampleObject(value = "Channel with id {channelId} not found")
          )
      ),
      @ApiResponse(
          responseCode = "204",
          description = "Channel이 성공적으로 삭제됨"
      )
  })
  @DeleteMapping(value = "{channelId}")
  public ResponseEntity<Void> delete(
      @Parameter(description = "삭제할 Channel ID", required = true)
      @PathVariable("channelId") UUID channelId) {
    channelService.delete(channelId);
    return ResponseEntity
        .status(HttpStatus.NO_CONTENT)
        .build();
  }
}
