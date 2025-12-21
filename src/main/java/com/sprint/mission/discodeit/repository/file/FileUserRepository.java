package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.util.FileIOUtil;
import org.springframework.stereotype.Repository; // 추가됨

import java.util.*;
import java.util.stream.Collectors;

@Repository // 1. 이 클래스를 Spring Bean(Repository)으로 등록합니다.
public class FileUserRepository implements UserRepository {

    // 2. Spring이 싱글톤 객체 생성을 보장하므로 수동 싱글톤 로직을 제거합니다.
    private static final String FILE_PATH = "data/user.json";

    // 3. 생성자를 public으로 변경하거나 생략하여 Spring이 제어할 수 있게 합니다.
    public FileUserRepository() {
    }

    // --- 파일 IO 유틸리티 ---
    private Map<UUID, User> readAll() {
        try {
            return FileIOUtil.readObjectFromFile(FILE_PATH);
        } catch (Exception e) {
            System.err.println("User 데이터 로딩 중 오류 발생: " + e.getMessage());
            return new HashMap<>();
        }
    }

    private void writeAll(Map<UUID, User> data) {
        try {
            FileIOUtil.writeObjectToFile(FILE_PATH, data);
        } catch (Exception e) {
            System.err.println("User 데이터 저장 중 오류 발생: " + e.getMessage());
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