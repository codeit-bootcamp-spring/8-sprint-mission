package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.DTO.response.BinaryContentResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf", matchIfMissing = true)
@Repository
public class JCFBinaryContentRepository implements BinaryContentRepository {

  private final Map<UUID, BinaryContent> data;

  public JCFBinaryContentRepository() {
        /*
        //기존에 사용했던 HashMap은 모든 HTTP 요청이 같은 HashMap에 접근하기 때문에 Tread-Safe 하지 못함
        //기본적으로 @Repository scope는 Singleton
        //Tread-Safe한 ConcurrentHashMap사용
        */
    this.data = new ConcurrentHashMap<>();
  }

  @Override
  public BinaryContent save(BinaryContent binaryContent) {
    data.put(binaryContent.getId(), binaryContent);
    return binaryContent;
  }

  @Override
  public Optional<BinaryContent> findById(UUID id) {
    return Optional.ofNullable(this.data.get(id));
  }

  @Override
  public List<UUID> findAll() {
    return data.keySet().stream().toList();
  }

  @Override
  public List<BinaryContent> findAllByIn(List<UUID> binaryContentIds) {
    if (binaryContentIds == null || binaryContentIds.isEmpty()) {
      return Collections.emptyList();
    }
    return binaryContentIds.stream()
        .map(this::findById)
        .flatMap(Optional::stream)
        .toList();
  }

  @Override
  public void deleteById(UUID id) {
    this.data.remove(id);
  }
}
