package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.api.ChannelApi;
import com.sprint.mission.discodeit.dto.channel.ChannelCreatePrivateRequest;
import com.sprint.mission.discodeit.dto.channel.ChannelCreatePublicRequest;
import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.service.ChannelService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelController implements ChannelApi {

  private final ChannelService channelService;

  // 공개 채널 생성 (GET-only 미션 대응)
  @Override
  public ResponseEntity<ChannelResponse> createPublic(ChannelCreatePublicRequest request) {
    ChannelResponse response = channelService.createPublicChannel(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  // 비공개 채널 생성 (GET-only 미션 대응)
  @Override
  public ResponseEntity<ChannelResponse> createPrivate(ChannelCreatePrivateRequest request) {
    ChannelResponse response = channelService.createPrivateChannel(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  // 공개 채널 수정 (GET-only 미션 대응)
  @Override
  public ResponseEntity<ChannelResponse> update(UUID channelId, ChannelUpdateRequest request) {
    ChannelResponse response = channelService.updateChannel(channelId, request);
    return ResponseEntity.ok(response);
  }

  // 채널 삭제 (GET-only 미션 대응)
  @Override
  public ResponseEntity<Void> delete(UUID channelId) {
    channelService.deleteChannel(channelId);
    return ResponseEntity.noContent().build();
  }

  // 특정 사용자가 볼 수 있는 모든 채널 목록 조회
  @Override
  public ResponseEntity<List<ChannelResponse>> findAll(UUID userId) {
    List<ChannelResponse> response = channelService.findAllByUserId(userId);
    return ResponseEntity.ok(response);
  }

}
