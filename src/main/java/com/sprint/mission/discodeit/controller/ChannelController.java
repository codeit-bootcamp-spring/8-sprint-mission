package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.DTO.dto.ChannelDto;
import com.sprint.mission.discodeit.DTO.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.DTO.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.DTO.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.controller.api.ChannelApi;
import com.sprint.mission.discodeit.service.ChannelService;
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

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/channels")
public class ChannelController implements ChannelApi {

  private final ChannelService channelService;

  @PostMapping("public")
  public ResponseEntity<ChannelDto> createPublicChannel(
      @RequestBody PublicChannelCreateRequest channelCreateRequest) {
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(channelService.create(channelCreateRequest));
  }

  @PostMapping("private")
  public ResponseEntity<ChannelDto> createPrivateChannel(
      @RequestBody PrivateChannelCreateRequest channelCreateRequest) {
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(channelService.create(channelCreateRequest));
  }

  @GetMapping
  public ResponseEntity<List<ChannelDto>> findAll(
      @RequestParam(value = "userId") UUID userId) {
    List<ChannelDto> channelResponse = channelService.findAll(userId);
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(channelResponse);
  }

  @PatchMapping("{channelId}")
  public ResponseEntity<ChannelDto> update(
      @PathVariable("channelId") UUID channelId,
      @RequestBody PublicChannelUpdateRequest channelUpdateRequest) {
    ChannelDto channelResponse = channelService.update(channelId, channelUpdateRequest);
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(channelResponse);
  }

  @DeleteMapping(value = "{channelId}")
  public ResponseEntity<Void> delete(
      @PathVariable("channelId") UUID channelId) {
    channelService.delete(channelId);
    return ResponseEntity
        .status(HttpStatus.NO_CONTENT)
        .build();
  }
}
