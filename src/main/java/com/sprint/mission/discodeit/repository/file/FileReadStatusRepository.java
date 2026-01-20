package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.util.FileUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileReadStatusRepository implements ReadStatusRepository {
    private final String filePath;

    public FileReadStatusRepository(@Value("${discodeit.repository.file-directory}") String directory) {
        this.filePath = directory + "/read-status.json";
    }

    @Override
    public ReadStatus save(ReadStatus readStatus) {
        List<ReadStatus> list = findAll();
        list.removeIf(e -> e.getId().equals(readStatus.getId()));
        list.add(readStatus);
        FileUtil.saveToFile(filePath, list);
        return readStatus;
    }

    @Override
    public Optional<ReadStatus> findById(UUID id) {
        return findAll().stream().filter(e -> e.getId().equals(id)).findFirst();
    }

    @Override
    public List<ReadStatus> findAll() {
        return FileUtil.readListFromFile(filePath, ReadStatus.class);
    }

    @Override
    public void delete(UUID id) {
        List<ReadStatus> list = findAll();
        list.removeIf(e -> e.getId().equals(id));
        FileUtil.saveToFile(filePath, list);
    }

    @Override
    public List<ReadStatus> findAllByUserId(UUID userId) {
        return findAll().stream()
                .filter(rs -> rs.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<ReadStatus> findAllByChannelId(UUID channelId) {
        return findAll().stream()
                .filter(rs -> rs.getChannelId().equals(channelId))
                .collect(Collectors.toList());
    }
}