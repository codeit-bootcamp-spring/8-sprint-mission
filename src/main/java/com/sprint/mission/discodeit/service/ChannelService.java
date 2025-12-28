package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.ChannelCreateRequest;
import com.sprint.mission.discodeit.dto.ChannelResponse;
import com.sprint.mission.discodeit.dto.ChannelUpdateRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChannelService {
    ChannelResponse createPublic(ChannelCreateRequest request);

    ChannelResponse create(ChannelCreateRequest request);

    ChannelResponse createPrivate(ChannelCreateRequest request);
    Optional<ChannelResponse> findById(UUID id);

    ChannelResponse create(String name, String description);

    List<ChannelResponse> findAll();

    // 에러 발생 지점: 이 메서드가 인터페이스에 있다면 구현체에도 반드시 있어야 합니다.
    List<ChannelResponse> findAllByUserId(UUID userId);

    ChannelResponse update(ChannelUpdateRequest request);

    ChannelResponse update(UUID id, String name, String description);

    void delete(UUID id);
}