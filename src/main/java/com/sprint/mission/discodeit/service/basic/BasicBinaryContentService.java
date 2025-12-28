package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

    private final BinaryContentRepository binaryContentRepository;


        return allContents.stream()
                .filter(content -> ids.contains(content.getId()))
                .collect(Collectors.toList());
    }
}