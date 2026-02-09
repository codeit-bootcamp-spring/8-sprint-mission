package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicBinaryContentService implements BinaryContentService {

  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentMapper binaryContentMapper;
  private final BinaryContentStorage binaryContentStorage;

  @Override
  @Transactional
  public BinaryContentDto create(BinaryContentCreateRequest request) {

    log.info("[BINARY_CONTENT] create start fileName={}", request.fileName());

    // 메타데이터
    BinaryContent binaryContent = new BinaryContent(
        request.fileName(),
        request.size(),
        request.contentType()
    );

    BinaryContent savedContent = binaryContentRepository.save(binaryContent);

    // 실제 bytes 저장
    binaryContentStorage.put(savedContent.getId(), request.bytes());

    log.info("[BINARY_CONTENT] create success binaryContentId={}", savedContent.getId());

    return binaryContentMapper.toDto(savedContent);
  }

  @Override
  public BinaryContentDto findById(UUID id) {
    return binaryContentRepository.findById(id)
        .map(binaryContentMapper::toDto)
        .orElseThrow(() -> new NoSuchElementException("BinaryContent를 찾을 수 없습니다." + id));
  }

  @Override
  public List<BinaryContentDto> findAllByIdIn(List<UUID> ids) {
    return binaryContentRepository.findAllById(ids).stream()
        .map(binaryContentMapper::toDto)
        .toList();
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    if (!binaryContentRepository.existsById(id)) {
      throw new NoSuchElementException("BinaryContent를 찾을 수 없습니다. " + id);
    }
    binaryContentRepository.deleteById(id);
  }
}
