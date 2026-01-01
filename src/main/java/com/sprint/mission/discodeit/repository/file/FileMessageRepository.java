package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
@Repository
public class FileMessageRepository implements MessageRepository {
    private final Path directory;

    public FileMessageRepository(
            @Value("${discodeit.repository.file-directory:data}") String fileDirectory
    ) {
        this.directory = Paths.get(System.getProperty("user.dir"), fileDirectory, Message.class.getSimpleName());

        // 저장할 경로의 파일 초기화
        if (!Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private Path resolvePath(UUID id) {
        return directory.resolve(id + ".ser");
    }

    @Override
    public Message save(Message message) {
        Path filePath = resolvePath(message.getId());

        try(
                FileOutputStream fos = new FileOutputStream(filePath.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos);
        ) {
            oos.writeObject(message);
        } catch (IOException e) {
            throw new RuntimeException("저장에 실패 했습니다: " + message.getFileName(), e);
        }
        return message;
    }

    public boolean checkExist(UUID id, Path filePath) {
        return Files.exists(filePath);
    }

    @Override
    public Optional<Message> findById(UUID id) {
        Message messageNullable = null;
        Path filePath = resolvePath(id);

        if(!checkExist(id, filePath)) return Optional.empty();
        else {
            try (FileInputStream fis = new FileInputStream(filePath.toFile());
                 ObjectInputStream ois = new ObjectInputStream(fis)
            ) {
                messageNullable = (Message) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException("메시지 조회 실패: " + id, e);
            }
        }
        return Optional.of(messageNullable);
    }

    @Override
    public Optional<Instant> findLatestMessageTimeByChannelId(UUID channelId) {
        try {
            return Files.list(directory)
                    .map(path -> {
                        try (
                            FileInputStream fis = new FileInputStream(path.toFile());
                            ObjectInputStream ois = new ObjectInputStream(fis);
                        ) {
                            return (Message) ois.readObject();
                        } catch (IOException | ClassNotFoundException e) {
                            throw new  RuntimeException(e);
                        }
                    })
                    .filter(message -> message.getChannelId().equals(channelId))
                    .map(Message::getCreatedAt)
                    .max(Comparator.naturalOrder());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Message> findAll() {
        try {
            return  Files.list(directory)
                    .map(path -> {
                        try (
                                FileInputStream fis = new FileInputStream(path.toFile());
                                ObjectInputStream ois = new ObjectInputStream(fis);
                        ) {
                            return (Message) ois.readObject();
                        } catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(UUID id) {
        Path path = resolvePath(id);
        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteAllByChannelId(UUID channelId) {
        try {
            Files.list(directory)
                    .forEach(path -> {
                        try (
                                FileInputStream fis = new FileInputStream(path.toFile());
                                ObjectInputStream ois = new ObjectInputStream(fis)
                        ) {
                            Message message = (Message) ois.readObject();
                            if (message.getChannelId().equals(channelId)) {
                                Files.delete(path);
                            }
                        } catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
