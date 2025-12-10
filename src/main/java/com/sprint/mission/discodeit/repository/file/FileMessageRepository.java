package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.MessageRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

public class FileMessageRepository implements MessageRepository {
    private final Path directory = Paths.get(System.getProperty("user.dir"), "data", "messages");

    public FileMessageRepository() {
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

    @Override
    public Message save(Message message) {
        message.setModifiedAt(message.getCreatedAt());
        Path filePath = directory.resolve(message.getFileName());

        try(
                FileOutputStream fos = new FileOutputStream(filePath.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos);
        ) {
            oos.writeObject(message);
            return message;
        } catch (IOException e) {
            throw new RuntimeException("저장에 실패 했습니다: " + message.getFileName(), e);
        }
    }

    public boolean checkExist(UUID id, Path filePath) {
        return Files.exists(filePath);
    }

    @Override
    public Optional<Message> findById(UUID id) {
        Path filePath = directory.resolve(id.toString().concat(".ser"));

        if(!checkExist(id, filePath)) return Optional.empty();
        else {
            try (FileInputStream fis = new FileInputStream(filePath.toFile());
                 ObjectInputStream ois = new ObjectInputStream(fis)
            ) {
                Message message = (Message) ois.readObject();
                return Optional.of(message);
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException("메시지 조회 실패: " + id, e);
            }
        }
    }
}
