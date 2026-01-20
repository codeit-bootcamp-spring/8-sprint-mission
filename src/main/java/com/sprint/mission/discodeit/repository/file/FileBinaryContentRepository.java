package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.util.FileUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileBinaryContentRepository implements BinaryContentRepository {

  private final String filePath;

  public FileBinaryContentRepository(
      @Value("${discodeit.repository.file-directory}") String directory) {
    this.filePath = directory + "/binary_contents.json";
  }

  @Override
  public BinaryContent save(BinaryContent binaryContent) {
    List<BinaryContent> list = findAll();
    // 기존에 동일한 ID가 있다면 제거하고 새로 추가 (업데이트 로직)
    list.removeIf(e -> e.getId().equals(binaryContent.getId()));
    list.add(binaryContent);

    // FileUtil을 사용하여 파일에 저장 (Thread.sleep 없음)
    FileUtil.saveToFile(filePath, list);
    return binaryContent;
  }

  @Override
  public Optional<BinaryContent> findById(UUID id) {
    return findAll().stream()
        .filter(e -> e.getId().equals(id))
        .findFirst();
  }

  @Override
  public List<BinaryContent> findAll() {
    return FileUtil.readListFromFile(filePath, BinaryContent.class);
  }

  @Override
  public void delete(UUID id) {
    List<BinaryContent> list = findAll();
    list.removeIf(e -> e.getId().equals(id));
    FileUtil.saveToFile(filePath, list);
  }
}