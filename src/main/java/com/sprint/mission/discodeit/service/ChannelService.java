package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.dto.ChannelDto;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface ChannelService {

    ChannelDto create(PublicChannelCreateRequest request, UUID userId);

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
