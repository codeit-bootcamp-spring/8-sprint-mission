package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class FileMessageRepository implements MessageRepository {

    private static FileMessageRepository INSTANCE;
    private static final String FILE_PATH = "repo_messages.dat";

    private FileMessageRepository() {}

    public static FileMessageRepository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FileMessageRepository();
        }
        return INSTANCE;
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