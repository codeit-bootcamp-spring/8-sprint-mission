package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/binaryContents")
@RequiredArgsConstructor
public class BinaryContentController {

    private final BinaryContentService binaryContentService;

    /**
     * 여러 첨부 파일 조회
     * GET /api/binaryContents?binaryContentIds=...
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<BinaryContent>> findAllByIds(@RequestParam List<UUID> binaryContentIds) {
        List<BinaryContent> contents = binaryContentService.findAllByIdIn(binaryContentIds);
        return ResponseEntity.ok(contents);
    }

    /**
     * 첨부 파일 조회
     * GET /api/binaryContents/{binaryContentId}
     */
    @RequestMapping(value = "/{binaryContentId}", method = RequestMethod.GET)
    public ResponseEntity<BinaryContent> findById(@PathVariable UUID binaryContentId) {
        BinaryContent content = binaryContentService.findById(binaryContentId)
                .orElseThrow(() -> new IllegalArgumentException("파일 정보를 찾을 수 없습니다."));

        return ResponseEntity.ok(content);
    }
}