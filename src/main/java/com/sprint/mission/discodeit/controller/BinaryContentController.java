package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponse;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/binary-content")
@RequiredArgsConstructor
public class BinaryContentController {

    private final BinaryContentService binaryContentService;

    // 바이너리 파일 단건 조회 (GET-only 미션 대응)
    @RequestMapping("find")
    public ResponseEntity<BinaryContentResponse> findOne(@RequestParam("id") UUID id) {
        BinaryContentResponse response = binaryContentService.findById(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    // 바이너리 파일 다건 조회 (GET-only 미션 대응)
    @RequestMapping("find-all")
    public ResponseEntity<List<BinaryContentResponse>> findAll(@RequestParam("ids") List<UUID> ids) {
        List<BinaryContentResponse> response = binaryContentService.findAllByIdIn(ids);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

}
