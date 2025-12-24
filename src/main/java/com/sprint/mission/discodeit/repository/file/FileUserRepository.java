package com.sprint.mission.discodeit.repository.file;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
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
public class FileUserRepository implements UserRepository {
    private final Path directory ;

    public FileUserRepository(
            @Value("${discodeit.repository.file-directory:data}") String fileDirectory
    ) {
        this.directory = Paths.get(System.getProperty("user.dir"), fileDirectory, User.class.getSimpleName());

        // 저장할 경로의 파일 초기화
        if (!Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Path resolvePath(UUID id) {
        return directory.resolve(id + ".ser");
    }

    @Override
    public User save(User user) {
        Path filePath = resolvePath(user.getId());

        try(
                FileOutputStream fos = new FileOutputStream(filePath.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos);
        ) {
            oos.writeObject(user);
        } catch (IOException e) {
            throw new RuntimeException("저장에 실패했습니다: " + resolvePath(user.getId()), e);
        }
        return user;
    }

    public boolean checkExist(Path filePath) {
        return Files.exists(filePath);
    }

    @Override
    public Optional<User> findById(UUID id) {
        User userNullable = null;
        Path filePath = resolvePath(id);

        if(!checkExist(filePath)) return Optional.empty();
        else {
            try (FileInputStream fis = new FileInputStream(filePath.toFile());
                 ObjectInputStream ois = new ObjectInputStream(fis)
            ) {
                userNullable = (User) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException("유저 조회 실패: " + id, e);
            }
        }
        return Optional.ofNullable(userNullable);
    }

    @Override
    public Optional<User> findByUsername(String name) {
        try {
            return Files.list(directory)
                    .map(path -> {
                        try(
                            FileInputStream fis = new FileInputStream(path.toFile());
                            ObjectInputStream ois = new ObjectInputStream(fis);
                        ) {
                            return (User) ois.readObject();
                        } catch(IOException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .filter(user -> user.getName().equals(name))
                    .findFirst();
        } catch (IOException e) {
            throw new RuntimeException(name  + "으로 사용자 조회 실패했습니다." , e);
        }
    }

    @Override
    public List<User> findAll() {
        try {
            return  Files.list(directory)
                    .map(path -> {
                        try(
                                FileInputStream fis = new FileInputStream(path.toFile());
                                ObjectInputStream ois = new ObjectInputStream(fis);
                        ) {
                            return (User) ois.readObject();
                        } catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
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

    @Override
    public boolean existsByName(String name) {
        try {
            return Files.list(directory)
                    .map(path -> {
                        try(
                                FileInputStream fis = new FileInputStream(path.toFile());
                                ObjectInputStream ois = new ObjectInputStream(fis);
                        ) {
                            return (User) ois.readObject();
                        } catch(IOException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .anyMatch(user -> user.getName().equals(name));
        } catch (IOException e) {
            throw new RuntimeException(name  + "으로 사용자 조회 실패했습니다." , e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        try {
            return Files.list(directory)
                    .map(path -> {
                        try(
                                FileInputStream fis = new FileInputStream(path.toFile());
                                ObjectInputStream ois = new ObjectInputStream(fis);
                        ) {
                            return (User) ois.readObject();
                        } catch(IOException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .anyMatch(user -> user.getEmail().equals(email));
        } catch (IOException e) {
            throw new RuntimeException(email  + "으로 사용자 조회 실패했습니다." , e);
        }
    }
}
