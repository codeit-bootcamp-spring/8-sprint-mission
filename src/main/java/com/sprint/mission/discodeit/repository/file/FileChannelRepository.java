package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.ChannelStatus;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
@Repository
public class FileChannelRepository implements ChannelRepository {
    private final Path directory ;

    public FileChannelRepository(
            @Value("${discodeit.repository.file-directory:data}") String fileDirectory
    ) {
        this.directory = Paths.get(System.getProperty("user.dir"), fileDirectory, Channel.class.getSimpleName());

        // 저장할 경로의 파일 초기화
        if (!Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Path resolovePath(UUID id) {
        return directory.resolve(id + ".ser");
    }

    @Override
    public Channel save(Channel channel) {
        Path filePath = directory.resolve(channel.getFileName());

        try(
                FileOutputStream fos = new FileOutputStream(filePath.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos);
        ) {
            oos.writeObject(channel);
        } catch (IOException e) {
            throw new RuntimeException("저장에 실패 했습니다: " + channel.getFileName(), e);
        }
        return channel;
    }

    public boolean checkExist(UUID id, Path filePath) {
        return Files.exists(filePath);
    }

    @Override
    public Optional<Channel> findById(UUID id) {
        Channel channelNullable = null;
        Path filePath = resolovePath(id);

        if(!checkExist(id, filePath)) return Optional.empty();
        else {
            try (FileInputStream fis = new FileInputStream(filePath.toFile());
                 ObjectInputStream ois = new ObjectInputStream(fis)
            ) {
                channelNullable = (Channel) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException("채널 조회 실패: " + id, e);
            }
        }
        return Optional.of(channelNullable);
    }

    @Override
    public List<Channel> findAll() {
        try {
            return Files.list(directory)
                    .map(path -> {
                        try (
                                FileInputStream fis = new FileInputStream(path.toFile());
                                ObjectInputStream ois = new ObjectInputStream(fis);
                        ) {
                            return (Channel) ois.readObject();
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
    public List<Channel> findAllByStatus(ChannelStatus status) {
        try {
            return Files.list(directory)
                    .map(path -> {
                        try (
                                FileInputStream fis = new FileInputStream(path.toFile());
                                ObjectInputStream ois = new ObjectInputStream(fis);
                        ) {
                            return (Channel) ois.readObject();
                        } catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .filter(channel -> channel.getStatus().equals(ChannelStatus.PUBLIC))
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(status + "로 채널을 조회할 수 없습니다.", e);
        }
    }

    @Override
    public void delete(UUID id) {
        Path path = resolovePath(id);
        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Channel> findAllPrivateChannelIdsByUserId(UUID uId) {
        try {
            return Files.list(directory)
                    .map(path -> {
                        try (
                                FileInputStream fis = new FileInputStream(path.toFile());
                                ObjectInputStream ois = new ObjectInputStream(fis);
                        ) {
                            return (Channel) ois.readObject();
                        } catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .filter(channel -> channel.getUserId().equals(uId) && channel.getStatus().equals(ChannelStatus.PRIVATE))
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(uId + "로 채널을 조회할 수 없습니다.", e);
        }
    }
}
