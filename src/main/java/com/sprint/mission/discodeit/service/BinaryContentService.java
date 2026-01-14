package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.DTO.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.DTO.response.BinaryContentResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface BinaryContentService {

  BinaryContentResponse create(BinaryContentCreateRequest request);

  BinaryContentResponse find(UUID id);

  List<BinaryContentResponse> findAllByIn(List<UUID> binaryContentIds);

  void delete(UUID id);

}
