package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.api.BinaryContentApi;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/binaryContents")
@RequiredArgsConstructor
public class BinaryContentController implements BinaryContentApi {

  private final BinaryContentService binaryContentService;
  private final BinaryContentStorage binaryContentStorage;

  // 바이너리 파일 단건 조회 (GET-only 미션 대응)
  @Override
  public ResponseEntity<BinaryContentDto> find(UUID binaryContentId) {
    return ResponseEntity.ok(binaryContentService.findById(binaryContentId));
  }

  // 추가 방식: 실제 파일 다운로드용
  @GetMapping("{binaryContentId}/download")
  public ResponseEntity<?> download(
      @PathVariable UUID binaryContentId) {

    BinaryContentDto dto = binaryContentService.findById(binaryContentId);

    return binaryContentStorage.download(dto);
  }


  // 바이너리 파일 다건 조회 (GET-only 미션 대응)
  @Override
  public ResponseEntity<List<BinaryContentDto>> findAllByIdIn(List<UUID> binaryContentIds) {
    return ResponseEntity.ok(binaryContentService.findAllByIdIn(binaryContentIds));
  }
}
