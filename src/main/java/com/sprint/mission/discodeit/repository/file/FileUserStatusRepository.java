package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
@Repository
public class FileUserStatusRepository implements UserStatusRepository {
    private final Path directory;

    public FileUserStatusRepository(
            @Value("${discodeit.repository.file-directory:data}") String fileDirectory
    ) {
        this.directory = Paths.get(System.getProperty("user.dir"), fileDirectory, UserStatus.class.getSimpleName());

        if(!Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Path resolvePath(UUID id) { return directory.resolve(id + ".ser"); }

    public boolean checkExist(Path filePath) { return Files.exists(filePath); }

    @Override
    public UserStatus save(UserStatus userStatus) {
        Path filePath = resolvePath(userStatus.getId());

        try (
                FileOutputStream fos = new FileOutputStream(filePath.toFile());
                ObjectOutputStream  oos = new ObjectOutputStream(fos);
        ) {
            oos.writeObject(userStatus);
        } catch (IOException e) {
            throw new RuntimeException("저장에 실패했습니다. " + resolvePath(userStatus.getId()), e);
        }
        return userStatus;
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        try {
            return Files.list(directory)
                    .map(path -> {
                        try (
                                FileInputStream fis = new FileInputStream(path.toFile());
                                ObjectInputStream ois = new ObjectInputStream(fis);
                        ) {
                            return (UserStatus) ois.readObject();
                        } catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .anyMatch(status -> status.getUserId().equals(userId));
        } catch (IOException e) {
            throw new RuntimeException(userId + "로 UserStatus를 조회할 수 없습니다.", e);
        }
    }

    @Override
    public Optional<UserStatus> findById(UUID id) {
        UserStatus userStatusNullable = null;
        Path filePath = resolvePath(id);

        if(!checkExist(filePath)) return Optional.empty();
        else {
            try (FileInputStream fis = new FileInputStream(filePath.toFile());
                 ObjectInputStream ois = new ObjectInputStream(fis);
            ) {
                userStatusNullable = (UserStatus) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException("UserStatus 조회 실패: " + id, e);
            }
        }
        return Optional.ofNullable(userStatusNullable);
    }

    @Override
    public Optional<UserStatus> findByUserId(UUID userId) {
        try {
            return Files.list(directory)
                    .map(path -> {
                        try (
                                FileInputStream fis = new FileInputStream(path.toFile());
                                ObjectInputStream ois = new ObjectInputStream(fis);
                        ) {
                            return (UserStatus) ois.readObject();
                        } catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .filter(userStatus -> userStatus.getUserId().equals(userId))
                    .findFirst();
        } catch (IOException e) {
            throw new RuntimeException(userId + "로 UserStatus를 조회할 수 없습니다.", e);
        }
    }

    @Override
    public List<UserStatus> findAll() {
        try {
            return Files.list(directory)
                    .map(path -> {
                        try (
                                FileInputStream fis = new FileInputStream(path.toFile());
                                ObjectInputStream ois = new ObjectInputStream(fis);
                        ) {
                            return (UserStatus) ois.readObject();
                        } catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();
        } catch (IOException e) {
            throw new  RuntimeException(e);
        }
    }

    @Override
    public void delete(UUID id) {
        Path path = resolvePath(id);
        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //파일을 읽고 스트림을 닫지 않은 상태에서 삭제를 요청해서 에러 발생..
    //스트림을 닫고 삭제 요청
    @Override
    public void deleteByUserId(UUID userId) {
        try {
            Files.list(directory)
                    .forEach(path -> {
                        UserStatus userStatus;
                        try (
                                FileInputStream fis = new FileInputStream(path.toFile());
                                ObjectInputStream ois = new ObjectInputStream(fis);
                        ) {
                            userStatus =  (UserStatus) ois.readObject();

                        } catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }

                        if(userStatus.getUserId().equals(userId)) {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
