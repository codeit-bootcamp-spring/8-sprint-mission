package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class FileUserService implements UserService {
    private final UserRepository userRepository;
    private final Path directory = Paths.get(System.getProperty("user.dir"), "data", "users");

    public FileUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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

    public static <T> void save(Path filePath, T data) {
        try(
                FileOutputStream fos = new FileOutputStream(filePath.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos);
        ) {
            oos.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> load(Path directory) {
        if (Files.exists(directory)) {
            try {
                List<T> list = Files.list(directory)
                        .map(path -> {
                            try (
                                    FileInputStream fis = new FileInputStream(path.toFile());
                                    ObjectInputStream ois = new ObjectInputStream(fis)
                            ) {
                                Object data = ois.readObject();
                                return (T) data;
                            } catch (IOException | ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .toList();
                return list;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public void createUser(User user) {
        user.setModifiedAt(user.getCreatedAt());
        Path filePath = directory.resolve(user.getFileName());
        save(filePath, user);
    }

    @Override
    public User create(String userId, String name, String email, char gender, int grade) {
        return new User(userId, name, email, gender, grade);
    }

    @Override
    public User findById(UUID id) {
        List<User> user = load(directory);
        return user.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<User> findUsers() {
        return load(directory);
    }

    @Override
    public Map<Integer, List<User>> findUserByGrade() {
        List<User> users = load(directory);
        return users.stream()
                .collect(
                        Collectors.groupingBy(
                                User::getGrade,
                                Collectors.toList()
                        ));
    }

    @Override
    public void updateUser(UUID id, String fieldName, Object value) {
        List<User> users = load(directory);
        Optional<User> optionalUser = users.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst();

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            if (fieldName.equals("userId")) {
                if (value instanceof String) {
                    user.setUserId((String) value);
                }
            } else if (fieldName.equals("name")) {
                if (value instanceof String) {
                    user.setName((String) value);
                }
            } else if (fieldName.equals("email")) {
                if (value instanceof String) {
                    user.setEmail((String) value);
                }
            } else if (fieldName.equals("gender")) {
                if (value instanceof Character) {
                    user.setGender((Character) value);
                }
            } else if (fieldName.equals("grade")) {
                if (value instanceof Integer) {
                    user.setGrade((Integer) value);
                }
            }

            user.setModifiedAt(System.currentTimeMillis());

            Path filePath = directory.resolve(user.getFileName());
            save(filePath, user);
        }
    }

    @Override
    public void deleteUser(UUID id) {
        List<User> users = load(directory);

        Optional<User> optionalUser = users.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst();
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            Path filePath = directory.resolve(user.getFileName());

            try {
                Files.deleteIfExists(filePath);
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Optional<User> findByUId(UUID id) {
        return userRepository.findByUserId(id);
    }

    @Override
    public void saveUser(User user) {
        if(findByUId(user.getId()).isEmpty()) {
            userRepository.save(user);
        } else {
            updateUser(user.getId(), "name", "박대기");
        }
    }
}
