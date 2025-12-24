package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.DTO.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.DTO.response.ReadStatusResponse;
import com.sprint.mission.discodeit.DTO.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.List;
import java.util.UUID;


public interface ReadStatusService {
    public ReadStatusResponse create(ReadStatusCreateRequest request);
    public ReadStatus find(UUID id);
    public List<ReadStatus> findAllByUserId(UUID uId);
    public ReadStatusResponse update(UUID id, ReadStatusUpdateRequest request);
    public void delete(UUID id);
}
