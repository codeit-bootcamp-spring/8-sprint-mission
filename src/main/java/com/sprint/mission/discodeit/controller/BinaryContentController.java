package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/binaryContent")
@RequiredArgsConstructor
public class BinaryContentController {

    private final BinaryContentRepository binaryContentRepository;

    /**
     * [심화] BinaryContent 파일 조회
     * URL: /api/binaryContent/find?binaryContentId=...
     */
    @RequestMapping(value = "/find", method = RequestMethod.GET)
    public ResponseEntity<BinaryContent> findById(@RequestParam UUID binaryContentId) {
        // DB에서 BinaryContent 정보를 조회합니다.
        BinaryContent content = binaryContentRepository.findById(binaryContentId)
                .orElseThrow(() -> new IllegalArgumentException("파일 정보를 찾을 수 없습니다."));

        return ResponseEntity.ok(content);
    }
}