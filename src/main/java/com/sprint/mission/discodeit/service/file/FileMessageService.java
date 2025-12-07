package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class FileMessageService implements MessageService {

    private static FileMessageService INSTANCE;
    private static final String FILE_PATH = "messages.dat";

    private FileMessageService() {
        // 싱글톤 패턴
    }

    public static FileMessageService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FileMessageService();
        }
        return INSTANCE;
    }

    // --- 파일 IO 유틸리티 (역직렬화) ---
    private Map<UUID, Message> readAll() {
        File file = new File(FILE_PATH);
        if (!file.exists() || file.length() == 0) {
            return new HashMap<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Map<UUID, Message>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Message 데이터 역직렬화 오류: " + e.getMessage());
            return new HashMap<>();
        }
    }

    // --- 파일 IO 유틸리티 (직렬화) ---
    private void writeAll(Map<UUID, Message> data) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(data);
        } catch (IOException e) {
            System.err.println("Message 데이터 직렬화 오류: " + e.getMessage());
        }
    }

    // --- MessageService 인터페이스 구현 (CRUD) ---

    @Override
    public Message save(Message message) {
        Map<UUID, Message> data = readAll();
        // **주의**: 현재 단계에서는 JCFService에 있던 User/Channel 검증 로직을 제외하고 순수 저장 로직만 구현합니다.
        // 해당 비즈니스 로직은 다음 단계에서 BasicMessageService로 분리될 예정입니다.
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
    public Message update(Message message) {
        Map<UUID, Message> data = readAll();
        if (data.containsKey(message.getId())) {
            data.put(message.getId(), message);
            writeAll(data);
            return message;
        }
        throw new NoSuchElementException("수정할 Message ID가 존재하지 않습니다: " + message.getId());
    }

    @Override
    public void delete(UUID id) {
        Map<UUID, Message> data = readAll();
        data.remove(id);
        writeAll(data);
    }
}