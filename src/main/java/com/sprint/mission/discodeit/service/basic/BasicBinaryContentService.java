package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicBinaryContentService {

    @Qualifier("JCFBinaryContentRepository")
    private final BinaryContentRepository binaryContentRepository;

    public List<BinaryContent> findAllByIds(List<UUID> ids) {
        //  1. findAll()이 리턴하는 List<BinaryContent>를 명시적으로 받습니다.
        List<BinaryContent> allContents = (List<BinaryContent>) binaryContentRepository.findAll();

        //  2. 이제 정상적으로 .stream()과 .filter()를 사용할 수 있습니다.
        return allContents.stream()
                .filter(content -> ids.contains(content.getId()))
                .collect(Collectors.toList());
    }
}