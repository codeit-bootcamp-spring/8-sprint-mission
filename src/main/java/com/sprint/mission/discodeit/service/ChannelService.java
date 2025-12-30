package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.channel.ChannelCreatePrivateRequest;
import com.sprint.mission.discodeit.dto.channel.ChannelCreatePublicRequest;
import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;

import java.util.List;
import java.util.UUID;

/*
    ChannelService
    -------------------------
    채널 관련 비즈니스 기능을 정의하는 인터페이스.
 */
public interface ChannelService {

  ChannelResponse createPublicChannel(ChannelCreatePublicRequest request);

  ChannelResponse createPrivateChannel(ChannelCreatePrivateRequest request);

  // 채널 조회 (단건)
  ChannelResponse findChannel(UUID id);

  // 채널 조회 (다건)
    /*
      특정 사용자가 볼 수 있는 채널 목록 조회.
      - PUBLIC 채널: 항상 포함
      - PRIVATE 채널: 해당 유저가 참여한 채널만 포함
     */
  List<ChannelResponse> findAllByUserId(UUID userId);

  // 수정
  ChannelResponse updateChannel(UUID channelId, ChannelUpdateRequest request);

  // 삭제
  void deleteChannel(UUID id);
}
