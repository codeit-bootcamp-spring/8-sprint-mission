package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Optional;
import java.util.UUID;

public class FileUserRepository implements UserRepository {
    private final Path directory = Paths.get(System.getProperty("user.dir"), "data", "users");

    public FileUserRepository() {
        init(directory);
    }

    public static void init(Path directory) {
        // 저장할 경로의 파일 초기화
        if (!Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public User save(User user) {
        user.setModifiedAt(user.getCreatedAt());
        Path filePath = directory.resolve(user.getFileName());

        try(
                FileOutputStream fos = new FileOutputStream(filePath.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos);
        ) {
            oos.writeObject(user);
            return user;
        } catch (IOException e) {
            throw new RuntimeException("저장에 실패 했습니다: " + user.getFileName(), e);
        }
    }

    public boolean checkExist(UUID id, Path filePath) {
        return Files.exists(filePath);
    }

    @Override
    public Optional<User> findByUserId(UUID id) {
        Path filePath = directory.resolve(id.toString().concat(".ser"));

        if(!checkExist(id, filePath)) return Optional.empty();
        else {
            try (FileInputStream fis = new FileInputStream(filePath.toFile());
                 ObjectInputStream ois = new ObjectInputStream(fis)
            ) {
                User user = (User) ois.readObject();
                return Optional.of(user);
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException("유저 조회 실패: " + id, e);
            }
        }
    }
}
