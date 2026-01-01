package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.DTO.request.ChannelCreateRequest;
import com.sprint.mission.discodeit.DTO.response.ChannelResponse;
import com.sprint.mission.discodeit.DTO.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ChannelService {
    ChannelResponse createPublic(ChannelCreateRequest request);
    ChannelResponse createPrivate(ChannelCreateRequest request);

    //단건 조회
    ChannelResponse findById(UUID id);
    //다건 조회
    //- 전체 조회
    List<ChannelResponse> findAll(UUID userId);
    //- 채널명별 조회
    Map<String, List<Channel>> findChannelByChannelName();
    //- TOP-N 채널 조회
    List<Channel> findChannelByTopNParticipant(int n);
    //- 채널에 있는 유저 오름차순 조회
    List<String> findChannelByParticipantsASC(UUID id);

    ChannelResponse update(UUID id, ChannelUpdateRequest request);

    //- 채널 삭제
    void delete(UUID id);
}
