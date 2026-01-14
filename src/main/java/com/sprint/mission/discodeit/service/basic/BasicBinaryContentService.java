package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.DTO.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.DTO.response.BinaryContentResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class BasicBinaryContentService implements BinaryContentService {

  private final BinaryContentRepository binaryContentRepository;

  @Override
  public BinaryContentResponse create(BinaryContentCreateRequest request) {
    BinaryContent binaryContent = new BinaryContent(
        request.fileName(),
        (long) request.bytes().length,
        request.contentType(),
        request.bytes()
    );

    binaryContentRepository.save(binaryContent);
    return BinaryContentResponse.from(binaryContent);
  }

  @Override
  public BinaryContentResponse find(UUID id) {
    BinaryContent binaryContent = binaryContentRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("BinaryContent not found"));

    return BinaryContentResponse.from(binaryContent);
  }

  @Override
  public List<BinaryContentResponse> findAllByIn(List<UUID> binaryContentIds) {
    return binaryContentRepository.findAllByIn(binaryContentIds).stream()
        .map(BinaryContentResponse::from)
        .toList();
  }

  public void delete(UUID id) {
    BinaryContent binaryContent = binaryContentRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("BinaryContent not found"));
    binaryContentRepository.deleteById(binaryContent.getId());
  }
}
