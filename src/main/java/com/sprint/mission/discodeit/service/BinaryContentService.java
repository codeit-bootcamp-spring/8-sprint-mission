package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import java.util.List;
import java.util.UUID;

/*
    BinaryContentService
    -------------------------
    파일/이미지 등 바이너리 데이터를 다루는 서비스

    BinaryContent 의 생성/조회/삭제만 담당한다.
 */
public interface BinaryContentService {

  BinaryContentDto create(BinaryContentCreateRequest request);

  BinaryContentDto findById(UUID id);

  List<BinaryContentDto> findAllByIdIn(List<UUID> ids);

  void delete(UUID id);
}
