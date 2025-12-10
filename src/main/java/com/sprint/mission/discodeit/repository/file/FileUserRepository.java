package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class FileUserRepository implements UserRepository {

    private static FileUserRepository INSTANCE;
    // [수정 후] data/ 폴더 안의 user.json을 바라보도록 경로를 수정합니다.
    private static final String FILE_PATH = "data/user.json";

    private FileUserRepository() {}

    public static FileUserRepository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FileUserRepository();
        }
        return INSTANCE;
    }

    // --- 파일 IO 유틸리티 (저장 로직) ---
    private Map<UUID, User> readAll() {
        File file = new File(FILE_PATH);
        if (!file.exists() || file.length() == 0) {
            return new HashMap<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Map<UUID, User>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("User Repository 역직렬화 오류: " + e.getMessage());
            return new HashMap<>();
        }
    }

    private void writeAll(Map<UUID, User> data) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(data);
        } catch (IOException e) {
            System.err.println("User Repository 직렬화 오류: " + e.getMessage());
        }
    }

    // --- Repository 인터페이스 구현 ---

    @Override
    public User save(User user) {
        Map<UUID, User> data = readAll();
        data.put(user.getId(), user);
        writeAll(data);
        return user;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(readAll().get(id));
    }

    @Override
    public List<User> findAll() {
        return readAll().values().stream().collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        Map<UUID, User> data = readAll();
        data.remove(id);
        writeAll(data);
    }
}