package com.sprint.mission.discodeit.api;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "BinaryContent", description = "첨부 파일 API")
public interface BinaryContentApi {

  @Operation(summary = "첨부 파일 조회")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "첨부 파일 조회 성공"),
      @ApiResponse(responseCode = "404", description = "첨부 파일을 찾을 수 없음",
          content = @Content(mediaType = "*/*",
              examples = @ExampleObject(value = "BinaryContent with id {binaryContentId} not found")
          ))
  })
  @GetMapping("/{binaryContentId}")
  ResponseEntity<BinaryContentResponse> find(
      @Parameter(description = "조회할 첨부 파일 ID", schema = @Schema(type = "string", format = "uuid"))
      @PathVariable UUID binaryContentId);

  @Operation(summary = "여러 첨부 파일 조회")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "첨부 파일 목록 조회 성공")
  })
  @GetMapping
  ResponseEntity<List<BinaryContentResponse>> findAllByIdIn(
      @Parameter(description = "조회할 첨부 파일 ID 목록",
          array = @ArraySchema(schema = @Schema(type = "string", format = "uuid")))
      @RequestParam List<UUID> binaryContentIds);
}
