package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
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
public class FileBinaryContentRepository implements BinaryContentRepository {
    private final Path directory;

    public FileBinaryContentRepository(
            @Value("${discodeit.repository.file-directory:data}") String fileDirectory
    ) {
        this.directory = Paths.get(System.getProperty("user.dir"), fileDirectory, BinaryContentRepository.class.getSimpleName());

        if (!Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                throw new  RuntimeException(e);
            }
        }
    }

    private Path resolvePath(UUID id) { return directory.resolve(id + ".ser"); }

    public boolean checkExist(Path filePath) { return Files.exists(filePath); }

    @Override
    public BinaryContent save(BinaryContent binaryContent) {
        Path filePath = resolvePath(binaryContent.getId());

        try (
                FileOutputStream fos = new FileOutputStream(filePath.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos);
        ) {
            oos.writeObject(binaryContent);
        } catch (IOException e) {
            throw new RuntimeException("저장에 실패했습니다. " + resolvePath(binaryContent.getId()), e);
        }
        return  binaryContent;
    }

    @Override
    public Optional<BinaryContent> findById(UUID id) {
        BinaryContent binaryContentNullable = null;
        Path filePath = resolvePath(id);

        if(!checkExist(filePath)) return Optional.empty();
        else {
            try (FileInputStream fis = new FileInputStream(filePath.toFile());
                 ObjectInputStream ois = new ObjectInputStream(fis);
            ) {
                binaryContentNullable = (BinaryContent) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException("BinaryContent 조회 실패: " + id , e);
            }
        }
        return Optional.ofNullable(binaryContentNullable);
    }

    @Override
    public List<UUID> findAll() {
        try {
            return Files.list(directory)
                    .map(path -> {
                        try (
                                FileInputStream fis = new FileInputStream(path.toFile());
                                ObjectInputStream ois = new ObjectInputStream(fis);
                        ) {
                            return (BinaryContent) ois.readObject();
                        } catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .map(BinaryContent::getId)
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

//    @Override
//    public void deleteByMessageId(UUID mId) {
//        try {
//            List<UUID> binaryContentIds = Files.list(directory)
//                .map(path -> {
//                    try (
//                            FileInputStream fis = new FileInputStream(path.toFile());
//                            ObjectInputStream ois = new ObjectInputStream(fis);
//                    ) {
//                        return (BinaryContent) ois.readObject();
//                    } catch (IOException | ClassNotFoundException e) {
//                        throw new RuntimeException(e);
//                    }
//                })
//                .filter(binaryContent -> binaryContent.getMid().equals(mId))
//                .map(BinaryContent::getId)
//                .toList();
//            for(UUID binaryContentId : binaryContentIds) {
//                Path path = resolvePath(binaryContentId);
//                try {
//                    Files.delete(path);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
}
