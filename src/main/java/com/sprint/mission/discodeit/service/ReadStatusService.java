package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.DTO.dto.ReadStatusDto;
import com.sprint.mission.discodeit.DTO.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.DTO.request.ReadStatusUpdateRequest;
import java.util.List;
import java.util.UUID;

public interface ReadStatusService {

  ReadStatusDto create(ReadStatusCreateRequest request);

  ReadStatusDto find(UUID id);

  List<ReadStatusDto> findAllByUserId(UUID userId);

  ReadStatusDto update(UUID readStatusId, ReadStatusUpdateRequest request);

  void delete(UUID id);
}
