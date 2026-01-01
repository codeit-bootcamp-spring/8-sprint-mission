package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/binaryContent")
@RequiredArgsConstructor
public class BinaryContentController {

    private final BinaryContentService binaryContentService;

    // 바이너리 파일 단건 조회 (GET-only 미션 대응)
    @RequestMapping("find")
    public ResponseEntity<BinaryContentResponse> findOne(@RequestParam("binaryContentId") UUID binaryContentId) {
        BinaryContentResponse response = binaryContentService.findById(binaryContentId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    // 바이너리 파일 다건 조회 (GET-only 미션 대응)
    @RequestMapping("findAll")
    public ResponseEntity<List<BinaryContentResponse>> findAll(@RequestParam("binaryContentIds") List<UUID> binaryContentIds) {
        List<BinaryContentResponse> response = binaryContentService.findAllByIdIn(binaryContentIds);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

}
