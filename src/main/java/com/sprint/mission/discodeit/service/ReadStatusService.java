package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusResponse;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;

import java.util.List;
import java.util.UUID;


/*
    ReadStatusService
    -------------------------
    ReadStatus(채널별 마지막 읽은 시각) 관련 비즈니스 로직을 정의
 */
public interface ReadStatusService {

    ReadStatusResponse create(ReadStatusCreateRequest request);

    ReadStatusResponse findById(UUID id);

    List<ReadStatusResponse> findAllByUserId(UUID userId);

    ReadStatusResponse update(ReadStatusUpdateRequest request);

    void delete(UUID id);
}
