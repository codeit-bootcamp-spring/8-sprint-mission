package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

public class FileChannelRepository implements ChannelRepository {
    private final Path directory = Paths.get(System.getProperty("user.dir"), "data", "channels");

    public FileChannelRepository() {
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
    public Channel save(Channel channel) {
        channel.setModifiedAt(channel.getCreatedAt());
        Path filePath = directory.resolve(channel.getFileName());

        try(
                FileOutputStream fos = new FileOutputStream(filePath.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos);
        ) {
            oos.writeObject(channel);
            return channel;
        } catch (IOException e) {
            throw new RuntimeException("저장에 실패 했습니다: " + channel.getFileName(), e);
        }
    }

    public boolean checkExist(UUID id, Path filePath) {
        return Files.exists(filePath);
    }

    @Override
    public Optional<Channel> findById(UUID id) {
        Path filePath = directory.resolve(id.toString().concat(".ser"));

        if(!checkExist(id, filePath)) return Optional.empty();
        else {
            try (FileInputStream fis = new FileInputStream(filePath.toFile());
                 ObjectInputStream ois = new ObjectInputStream(fis)
            ) {
                Channel channel = (Channel) ois.readObject();
                return Optional.of(channel);
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException("채널 조회 실패: " + id, e);
            }
        }
    }
}
