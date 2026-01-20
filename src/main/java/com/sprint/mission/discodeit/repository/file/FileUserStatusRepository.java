package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.util.FileUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileUserStatusRepository implements UserStatusRepository {
    private final String filePath;

    public FileUserStatusRepository(@Value("${discodeit.repository.file-directory}") String directory) {
        this.filePath = directory + "/user-status.json";
    }

    @Override
    public UserStatus save(UserStatus userStatus) {
        List<UserStatus> list = findAll();
        if (userStatus.getId() != null) {
            list.removeIf(e -> e != null && e.getId() != null && e.getId().equals(userStatus.getId()));
        }
        list.add(userStatus);
        FileUtil.saveToFile(filePath, list);
        return userStatus;
    }

    @Override
    public Optional<UserStatus> findById(UUID id) {
        return findAll().stream()
                .filter(e -> e != null && e.getId() != null && e.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<UserStatus> findAll() {
        return FileUtil.readListFromFile(filePath, UserStatus.class);
    }

    @Override
    public void delete(UUID id) {
        List<UserStatus> list = findAll();
        list.removeIf(e -> e != null && e.getId() != null && e.getId().equals(id));
        FileUtil.saveToFile(filePath, list);
    }

    @Override
    public Optional<UserStatus> findByUserId(UUID userId) {
        return findAll().stream()
                .filter(s -> s != null && s.getUserId() != null && s.getUserId().equals(userId))
                .findFirst();
    }
}