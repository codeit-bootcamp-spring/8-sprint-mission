package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.api.BinaryContentApi;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponse;
import com.sprint.mission.discodeit.service.BinaryContentService;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

@RestController
@RequestMapping("/api/binaryContents")
@RequiredArgsConstructor
public class BinaryContentController implements BinaryContentApi {

  private final BinaryContentService binaryContentService;

  // 바이너리 파일 단건 조회 (GET-only 미션 대응)
  @Override
  public ResponseEntity<BinaryContentResponse> find(UUID binaryContentId) {
    BinaryContentResponse response = binaryContentService.findById(binaryContentId);
    return ResponseEntity.ok(response);
  }

  // 추가 방식: 실제 파일 다운로드용
  @GetMapping("{binaryContentId}/download")
  public ResponseEntity<Resource> download(
      @PathVariable UUID binaryContentId) {

    BinaryContentResponse response = binaryContentService.findById(binaryContentId);

    byte[] fileBytes = response.bytes();
    Resource resource = new ByteArrayResource(fileBytes);

    // 파일명 인코딩
    String encodedFileName = UriUtils.encode(response.fileName(), StandardCharsets.UTF_8);

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(response.contentType()))
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName)
        .body(resource);
  }


  // 바이너리 파일 다건 조회 (GET-only 미션 대응)
  @Override
  public ResponseEntity<List<BinaryContentResponse>> findAllByIdIn(List<UUID> binaryContentIds) {
    List<BinaryContentResponse> response = binaryContentService.findAllByIdIn(binaryContentIds);
    return ResponseEntity.ok(response);
  }

}
