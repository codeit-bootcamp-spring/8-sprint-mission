package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponse;
import com.sprint.mission.discodeit.service.BinaryContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

@Tag(name = "BinaryContent", description = "첨부 파일 API")
@RestController
@RequestMapping("/api/binaryContents")
@RequiredArgsConstructor
public class BinaryContentController {

  private final BinaryContentService binaryContentService;

  // 바이너리 파일 단건 조회 (GET-only 미션 대응)
  @Operation(summary = "첨부 파일 조회")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "첨부 파일 조회 성공"),
      @ApiResponse(responseCode = "404", description = "첨부 파일을 찾을 수 없음",
          content = @Content(
              mediaType = "*/*",
              examples = @ExampleObject(value = "BinaryContent with id {binaryContentId} not found")
          ))
  })
  @GetMapping("{binaryContentId}")
  public ResponseEntity<BinaryContentResponse> find(
      @Parameter(
          description = "조회할 첨부 파일 ID",
          schema = @Schema(type = "string", format = "uuid")
      )
      @PathVariable UUID binaryContentId) {

    BinaryContentResponse response = binaryContentService.findById(binaryContentId);

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(response);
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
  @Operation(summary = "여러 첨부 파일 조회")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "첨부 파일 목록 조회 성공")
  })
  @GetMapping
  public ResponseEntity<List<BinaryContentResponse>> findAllByIdIn(
      @Parameter(
          description = "조회할 첨부 파일 ID 목록",
          array = @ArraySchema(
              schema = @Schema(type = "string", format = "uuid")
          )
      )
      @RequestParam List<UUID> binaryContentIds) {

    List<BinaryContentResponse> response = binaryContentService.findAllByIdIn(binaryContentIds);

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(response);
  }

}
