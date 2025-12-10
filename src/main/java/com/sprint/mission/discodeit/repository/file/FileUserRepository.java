package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.util.FileIOUtil; // ✨ FileIOUtil import
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class FileUserRepository implements UserRepository {

    private static FileUserRepository INSTANCE;
    // 경로를 'data/' 폴더 내부로 수정
    private static final String FILE_PATH = "data/user.json";
    // 파일 직렬화를 사용하므로 파일 확장자를 .dat 또는 .ser로 변경하는 것이 더 일반적일 수 있으나,
    // 현재는 .json 경로를 유지하며 직렬화 로직을 사용합니다.

    private FileUserRepository() {}

    public static FileUserRepository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FileUserRepository();
        }
        return INSTANCE;
    }

    // --- 파일 IO 유틸리티 (중복 코드 제거) --> 제미나이 도움을 받아 readALL을 사용하여 중복을 최소화했습니다 ㅎㅎ.. ---
    private Map<UUID, User> readAll() {
        //  FileIOUtil의 정적 메서드를 호출하여 IO 로직을 대체 및 간소화
        try {
            // FileIOUtil이 Java 직렬화 Map<UUID, User>을 반환한다고 가정
            return FileIOUtil.readObjectFromFile(FILE_PATH);
        } catch (Exception e) {
            // 실패 시 빈 Map 반환
            System.err.println("User 데이터 로딩 중 오류 발생: " + e.getMessage());
            return new HashMap<>();
        }
    }

    private void writeAll(Map<UUID, User> data) {
        // ✨ FileIOUtil의 정적 메서드를 호출하여 IO 로직을 대체 및 간소화
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