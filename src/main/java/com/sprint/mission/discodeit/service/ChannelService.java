package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.DTO.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.DTO.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.DTO.dto.ChannelDto;
import com.sprint.mission.discodeit.DTO.request.PublicChannelUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface ChannelService {

  ChannelDto create(PublicChannelCreateRequest request);

  ChannelDto create(PrivateChannelCreateRequest request);

  //단건 조회
  ChannelDto find(UUID channelId);

  //다건 조회
  //- 전체 조회
  List<ChannelDto> findAll(UUID userId);

  ChannelDto update(UUID channelId, PublicChannelUpdateRequest request);

  //- 채널 삭제
  void delete(UUID channelId);
}
