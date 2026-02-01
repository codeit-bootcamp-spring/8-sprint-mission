package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.DTO.dto.BinaryContentDto;
import com.sprint.mission.discodeit.DTO.request.BinaryContentCreateRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

public interface BinaryContentService {

  BinaryContentDto create(BinaryContentCreateRequest request);

  BinaryContentDto find(UUID id);

  List<BinaryContentDto> findAllByIn(List<UUID> binaryContentIds);

  void delete(UUID binaryContentId);
}
