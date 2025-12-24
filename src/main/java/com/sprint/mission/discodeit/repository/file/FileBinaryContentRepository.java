package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileBinaryContentRepository implements BinaryContentRepository {

    private final Path directory;
    private final String EXTENSION = ".ser";

    public FileBinaryContentRepository(
            @Value("${discodeit.repository.file-directory:.discodeit}") String rootDir
    ) {
        this.directory = Paths.get(rootDir, "BinaryContent");
        try {
            Files.createDirectories(this.directory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Path resolvePath(UUID id) {
        return directory.resolve(id.toString() + EXTENSION);
    }

    @Override
    public BinaryContent save(BinaryContent binaryContent) {
        Path path = resolvePath(binaryContent.getId());
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(path))) {
            oos.writeObject(binaryContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return binaryContent;
    }

    @Override
    public Optional<BinaryContent> findById(UUID id) {
        Path path = resolvePath(id);
        if (!Files.exists(path)) {
            return Optional.empty();
        }
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(path))) {
            return Optional.of((BinaryContent) ois.readObject());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // 인터페이스엔 선언 되있지 않지만 findAllByIdIn 이전에 전체
    // 데이터를 List로 읽어오기 위한 메서드
    private List<BinaryContent> findAll() {
        List<BinaryContent> result = new ArrayList<>();
        try {
            if (!Files.exists(directory)) {
                return result;
            }
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, "*" + EXTENSION)) {
                for (Path path : stream) {
                    try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(path))) {
                        result.add((BinaryContent) ois.readObject());
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public List<BinaryContent> findAllByIdIn(List<UUID> ids) {
        List<BinaryContent> all = findAll();
        List<BinaryContent> result = new ArrayList<>();
        for (UUID id : ids) {
            for (BinaryContent bc : all) {
                if (Objects.equals(bc.getId(), id)) {
                    result.add(bc);
                }
            }
        }
        return result;
    }

    @Override
    public void deleteById(UUID id) {
        try {
            Files.deleteIfExists(resolvePath(id));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteAllByIdIn(List<UUID> ids) {
        for (UUID id : ids) {
            deleteById(id);
        }
    }
}
