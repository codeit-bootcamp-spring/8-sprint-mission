package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class FileUserService implements UserService {

    private static FileUserService INSTANCE;

    private static final String FILE_PATH = "users.dat";

    private FileUserService() {
        // 싱글톤 패턴
    }

    public static FileUserService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FileUserService();
        }
        return INSTANCE;
    }

    // --- 파일 IO 유틸리티 (역직렬화) ---
    private Map<UUID, User> readAll() {
        File file = new File(FILE_PATH);
        if (!file.exists() || file.length() == 0) {
            return new HashMap<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            // 파일에서 Map<UUID, User> 객체를 읽어옵니다.
            return (Map<UUID, User>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("User 데이터 역직렬화 오류: " + e.getMessage());
            return new HashMap<>();
        }
    }

    // --- 파일 IO 유틸리티 (직렬화) ---
    private void writeAll(Map<UUID, User> data) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(data); // Map 객체를 통째로 파일에 씁니다.
        } catch (IOException e) {
            System.err.println("User 데이터 직렬화 오류: " + e.getMessage());
        }
    }

    // --- UserService 인터페이스 구현 (CRUD) ---

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
    public User update(User user) {
        Map<UUID, User> data = readAll();
        if (data.containsKey(user.getId())) {
            data.put(user.getId(), user);
            writeAll(data);
            return user;
        }
        throw new NoSuchElementException("수정할 User ID가 존재하지 않습니다: " + user.getId());
    }

    @Override
    public void delete(UUID id) {
        Map<UUID, User> data = readAll();
        data.remove(id);
        writeAll(data);
    }
}