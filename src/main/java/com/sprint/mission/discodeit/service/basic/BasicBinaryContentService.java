package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {

  private final BinaryContentRepository binaryContentRepository;

  @Override
  public BinaryContentDto create(BinaryContentCreateRequest request) {
    BinaryContent binaryContent = new BinaryContent(
        request.fileName(),
        request.contentType(),
        request.bytes()
    );
    binaryContentRepository.save(binaryContent);
    return convertDto(binaryContent);
  }

  @Override
  public BinaryContentDto findById(UUID id) {
    BinaryContent binaryContent = binaryContentRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("BinaryContent를 찾을 수 없습니다." + id));
    return convertDto(binaryContent);
  }

  @Override
  public List<BinaryContentDto> findAllByIdIn(List<UUID> ids) {
    return binaryContentRepository.findAllByIdIn(ids).stream()
        .map(binaryContent -> this.convertDto(binaryContent))
        .collect(Collectors.toList());
  }

  @Override
  public void delete(UUID id) {
    binaryContentRepository.deleteById(id);
  }

  private BinaryContentDto convertDto(BinaryContent binaryContent) {

    // 파일 저장소에서 옛날 데이터 읽어온 경우 contentType이 null일 수 있음 → 안전 처리
    String contentType = binaryContent.getContentType();
    if (contentType == null || contentType.isBlank()) {
      contentType = guessContentType(binaryContent.getFileName());
    }
    return new BinaryContentDto(
        binaryContent.getId(),
        binaryContent.getFileName(),
        contentType,
        binaryContent.getBytes(),
        binaryContent.getCreatedAt()
    );
  }

  private String guessContentType(String fileName) {
    if (fileName == null) {
      return "application/octet-stream";
    }
    String lower = fileName.toLowerCase();
    if (lower.endsWith(".png")) {
      return "image/png";
    }
    if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
      return "image/jpeg";
    }
    if (lower.endsWith(".gif")) {
      return "image/gif";
    }
    if (lower.endsWith(".webp")) {
      return "image/webp";
    }
    return "application/octet-stream";
  }
}
