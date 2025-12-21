package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import org.springframework.stereotype.Repository; // 추가됨

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Repository // 1. Spring이 이 클래스를 Repository Bean으로 관리하도록 등록합니다.
public class FileMessageRepository implements MessageRepository {

    // 2. Spring이 싱글톤 객체 생성을 보장하므로 static INSTANCE 필드와 getInstance()를 제거합니다.
    private static final String FILE_PATH = "data/message.json";

    // 3. 생성자를 public으로 변경하여 Spring이 객체를 생성할 수 있게 합니다. (혹은 생략 가능)
    public FileMessageRepository() {
        // 데이터 저장용 디렉토리가 없다면 생성하는 로직을 추가하면 더 안전합니다.
        File directory = new File("data");
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    // --- 파일 IO 유틸리티 ---
    private Map<UUID, Message> readAll() {
        File file = new File(FILE_PATH);
        if (!file.exists() || file.length() == 0) {
            return new HashMap<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Map<UUID, Message>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Message Repository 역직렬화 오류: " + e.getMessage());
            return new HashMap<>();
        }
    }

    private void writeAll(Map<UUID, Message> data) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(data);
        } catch (IOException e) {
            System.err.println("Message Repository 직렬화 오류: " + e.getMessage());
        }
    }

    // --- Repository 인터페이스 구현 ---

    @Override
    public Message save(Message message) {
        Map<UUID, Message> data = readAll();
        data.put(message.getId(), message);
        writeAll(data);
        return message;
    }

    @Override
    public Optional<Message> findById(UUID id) {
        return Optional.ofNullable(readAll().get(id));
    }

    @Override
    public List<Message> findAll() {
        return readAll().values().stream().collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        Map<UUID, Message> data = readAll();
        data.remove(id);
        writeAll(data);
    }
}