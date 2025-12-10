package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface ChannelService {
    //채널 생성
    void createChannel(Channel channel);

    Channel create(UUID uid, String name, String host, int participant, List<String> participants);

    //단건 조회
    Channel findById(UUID id);

    //다건 조회
    //- 전체 조회
    List<Channel> findChannels();
    //- 채널명별 조회
    Map<String, List<Channel>> findChannelByName();
    //- TOP-N 채널 조회
    List<Channel> findChannelByTopNParticipant(int n);
    //- 채널에 있는 유저 오름차순 조회
    List<String> findChannelByParticipantsASC(UUID id);

    //- 채널 수정
    void updateChannel(UUID id, String fieldName, String value);

    //- 채널 삭제
    void deleteChannel(UUID id);

    //채널 조회(FileChannelRepository)
    Optional<Channel> findByCId(UUID id);

    //채널 저장
    void saveChannel(Channel channel);
}
