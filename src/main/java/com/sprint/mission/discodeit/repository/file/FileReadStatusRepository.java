package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
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
public class FileReadStatusRepository implements ReadStatusRepository {
    private final Path directory;

    public FileReadStatusRepository(
            @Value("${discodeit.repository.file-directory:data}") String fileDirectory
    ) {
        this.directory = Paths.get(System.getProperty("user.dir"), fileDirectory, ReadStatus.class.getSimpleName());

        if(!Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Path resolvePath(UUID id) { return directory.resolve(id + ".ser"); }

    public boolean checkExist(Path filePath) { return Files.exists(filePath); }

    @Override
    public ReadStatus save(ReadStatus readStatus) {
        Path filePath = resolvePath(readStatus.getId());

        try (
                FileOutputStream fos = new FileOutputStream(filePath.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos);
        ) {
            oos.writeObject(readStatus);
        } catch (IOException e) {
            throw new RuntimeException("저장에 실패했습니다. " + resolvePath(readStatus.getId()) ,e);
        }
        return readStatus;
    }

    @Override
    public Optional<ReadStatus> findById(UUID id) {
        ReadStatus readStatusNullable = null;
        Path filePath = resolvePath(id);

        if(!checkExist(filePath)) return Optional.empty();
        else {
            try (FileInputStream fis = new FileInputStream(filePath.toFile());
                 ObjectInputStream ois = new ObjectInputStream(fis);
            ) {
                readStatusNullable = (ReadStatus) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException("ReadStatus 조회 실패: " + id, e);
            }
        }
        return Optional.ofNullable(readStatusNullable);
    }

    @Override
    public Optional<ReadStatus> findByChannelId(UUID cId) {
        try {
            return Files.list(directory)
                    .map(path -> {
                        try (
                            FileInputStream fis = new FileInputStream(path.toFile());
                            ObjectInputStream ois = new ObjectInputStream(fis);
                        ) {
                            return (ReadStatus) ois.readObject();
                        } catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .filter(readStatus -> readStatus.getCId().equals(cId))
                    .findFirst();
        }  catch (IOException e) {
            throw new RuntimeException(cId + "로 ReadStatus를 조회할 수 없습니다.");
        }
    }

    @Override
    public boolean existsByUserIdAndChannelId(UUID uId, UUID cId) {
        try {
            return Files.list(directory)
                    .map(path -> {
                        try (
                                FileInputStream fis = new FileInputStream(path.toFile());
                                ObjectInputStream ois = new ObjectInputStream(fis);
                        ) {
                            return (ReadStatus) ois.readObject();
                        } catch (IOException | ClassNotFoundException e) {
                            throw new  RuntimeException(e);
                        }
                    })
                    .anyMatch(readStatus -> readStatus.getUId().equals(uId) &&
                                                        readStatus.getCId().equals(cId));

        } catch (IOException e) {
            throw new RuntimeException(uId + "&" + cId + "로 ReadStatus를 조회할 수  없습니다. ", e);
        }
    }

    @Override
    public List<UUID> findAllByChannelId(UUID cId) {
        try {
            return Files.list(directory)
                    .map(path -> {
                        try (
                                FileInputStream fis = new FileInputStream(path.toFile());
                                ObjectInputStream ois = new ObjectInputStream(fis);
                        ) {
                            return (ReadStatus) ois.readObject();
                        } catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .map(ReadStatus::getCId)
                    .filter(readStatus -> readStatus.equals(cId))
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(cId + "로 ReadStatus를 조회할 수 없습니다.", e);
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
    public void deleteAllByChannelId(UUID cId) {
        try {
            List<UUID> readStatusIds = Files.list(directory)
                    .map(path -> {
                        try (
                                FileInputStream fis = new FileInputStream(path.toFile());
                                ObjectInputStream ois = new ObjectInputStream(fis);
                        ) {
                            return (ReadStatus) ois.readObject();
                        } catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .filter(readStatus -> readStatus.getCId().equals(cId))
                    .map(ReadStatus::getId)
                    .toList();
            for (UUID readStatusId : readStatusIds) {
                Path path = resolvePath(readStatusId);
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(cId + "로 ReadStatus를 조회할 수 없습니다.", e);
        }
    }

    @Override
    public List<ReadStatus> findAllByUserId(UUID uId) {
        try {
            return Files.list(directory)
                    .map(path -> {
                        try (
                                FileInputStream fis = new FileInputStream(path.toFile());
                                ObjectInputStream ois = new ObjectInputStream(fis);
                        ) {
                            return (ReadStatus) ois.readObject();
                        } catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .filter(readStatus -> readStatus.getUId().equals(uId))
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
