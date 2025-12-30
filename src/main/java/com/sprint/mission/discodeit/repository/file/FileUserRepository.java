package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileUserRepository implements UserRepository {

  private final Path directory;
  private static final String EXTENSION = ".ser";

  public FileUserRepository(
      @Value("${discodeit.repository.file-directory:.discodeit}") String rootDir
  ) {
    this.directory = Paths.get(rootDir, "User");
    try {
      Files.createDirectories(this.directory);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // 저장할 데이터를 담을 '전용 박스'에 고유한 주소 라벨 붙이는 것
  // 이전에 설정한 User 저장 디렉토리에 이 파일 이름을 합쳐서 (id + 확장자)
  // 최종 저장 경로(Path 객체)를 완성
  private Path resolvePath(UUID id) {
    return directory.resolve(id.toString() + EXTENSION);
  }

  @Override
  public User save(User user) {
    Path path = resolvePath(user.getId());
    try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(path))) {
      oos.writeObject(user);
    } catch (IOException e) {
      throw new RuntimeException("생성에 실패 했습니다. " + this.directory, e);
    }
    return user;
  }

  @Override
  public Optional<User> findById(UUID id) {
    Path path = resolvePath(id);
    if (!Files.exists(path)) {
      return Optional.empty();
    }
    try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(path))) {
      return Optional.of((User) ois.readObject());
    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<User> findAll() {
    List<User> result = new ArrayList<>();
    try {
      if (!Files.exists(directory)) {
        return result;
      }
      try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, "*" + EXTENSION)) {
        for (Path path : stream) {
          try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(path))) {
            result.add((User) ois.readObject());
          }
        }
      }
    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    return result;
  }

  @Override
  public boolean existsById(UUID id) {
    return Files.exists(resolvePath(id));
  }

  @Override
  public void delete(UUID id) {
    try {
      Files.deleteIfExists(resolvePath(id));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Optional<User> findByUsername(String username) {
    return findAll().stream()
        .filter(u -> Objects.equals(u.getName(), username))
        .findFirst();
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return findAll().stream()
        .filter(u -> Objects.equals(u.getEmail(), email))
        .findFirst();
  }

  @Override
  public boolean existsByUsernameOrEmail(String username, String email) {
    return findAll().stream()
        .anyMatch(u -> Objects.equals(u.getName(), username)
            || Objects.equals(u.getEmail(), email));
  }


}
