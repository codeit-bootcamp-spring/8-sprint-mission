package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.util.FileUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileUserRepository implements UserRepository {
    private final String filePath;

    public FileUserRepository(@Value("${discodeit.repository.file-directory}") String directory) {
        this.filePath = directory + "/user.json";
    }

    @Override
    public User save(User user) {
        List<User> list = findAll();
        list.removeIf(e -> e.getId().equals(user.getId()));
        list.add(user);
        FileUtil.saveToFile(filePath, list);
        return user;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return findAll().stream().filter(e -> e.getId().equals(id)).findFirst();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return findAll().stream()
                .filter(e -> e.getEmail() != null && e.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public List<User> findAll() {
        return FileUtil.readListFromFile(filePath, User.class);
    }

    @Override
    public void delete(UUID id) {
        List<User> list = findAll();
        list.removeIf(e -> e.getId().equals(id));
        FileUtil.saveToFile(filePath, list);
    }

    @Override
    public boolean existsByName(String name) {
        return findAll().stream()
                .anyMatch(e -> e.getUsername() != null && e.getUsername().equals(name));
    }

    @Override
    public boolean existsByEmail(String email) {
        return findAll().stream()
                .anyMatch(e -> e.getEmail() != null && e.getEmail().equals(email));
    }
}