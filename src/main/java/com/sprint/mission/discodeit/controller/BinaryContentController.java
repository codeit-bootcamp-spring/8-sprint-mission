package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/binary-contents")
@RequiredArgsConstructor
public class BinaryContentController {

    private final BinaryContentRepository binaryContentRepository;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public BinaryContent findById(@PathVariable UUID id) {
        return binaryContentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다."));
    }
}