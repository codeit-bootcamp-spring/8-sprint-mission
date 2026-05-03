package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.api.BinaryContentApi;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
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

    log.info("[BINARY_CONTENT] download start binaryContentId={}", binaryContentId);

    BinaryContentDto dto = binaryContentService.findById(binaryContentId);

    if (dto.status() == BinaryContentStatus.PROCESSING) {
      log.warn("[BINARY_CONTENT] file still uploading binaryContentId={}", binaryContentId);
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
          .header("Retry-After", "3")
          .body("파일이 아직 업로드 중입니다. 잠시 후 다시 시도해주세요.");
    }

    if (dto.status() == BinaryContentStatus.FAIL) {
      log.error("[BINARY_CONTENT] file upload failed binaryContentId={}", binaryContentId);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("파일 업로드에 실패했습니다.");
    }

    log.info("[BINARY_CONTENT] download success binaryContentId={}", binaryContentId);
    return binaryContentStorage.download(dto);
  }


  // 바이너리 파일 다건 조회 (GET-only 미션 대응)
  @Override
  public ResponseEntity<List<BinaryContentDto>> findAllByIdIn(List<UUID> binaryContentIds) {
    return ResponseEntity.ok(binaryContentService.findAllByIdIn(binaryContentIds));
  }
}
