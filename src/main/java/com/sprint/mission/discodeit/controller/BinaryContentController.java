package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/binaryContent")
@RequiredArgsConstructor
public class BinaryContentController {

    private final BinaryContentService binaryContentService;

    /**
     * BinaryContent 생성
     * POST /api/binaryContent
     */
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<BinaryContent> create(@RequestBody BinaryContentCreateRequest request) {
        BinaryContent binaryContent = binaryContentService.create(request);
        return ResponseEntity.ok(binaryContent);
    }

    /**
     * [심화] BinaryContent 1개 조회
     * URL: /api/binaryContent/find?binaryContentId=...
     */
    @RequestMapping(value = "/find", method = RequestMethod.GET)
    public ResponseEntity<BinaryContent> findById(@RequestParam UUID binaryContentId) {
        BinaryContent content = binaryContentService.findById(binaryContentId)
                .orElseThrow(() -> new IllegalArgumentException("파일 정보를 찾을 수 없습니다."));

        return ResponseEntity.ok(content);
    }

    /**
     * [심화] BinaryContent 여러개 조회
     * URL: /api/binaryContent/findAll?ids=...
     */
    @RequestMapping(value = "/findAll", method = RequestMethod.GET)
    public ResponseEntity<List<BinaryContent>> findAllByIds(@RequestParam List<UUID> ids) {
        List<BinaryContent> contents = binaryContentService.findAllByIdIn(ids);
        return ResponseEntity.ok(contents);
    }
}