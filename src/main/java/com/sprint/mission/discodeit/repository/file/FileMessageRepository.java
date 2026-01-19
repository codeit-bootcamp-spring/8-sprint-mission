package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.util.FileUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileMessageRepository implements MessageRepository {
    private final String filePath;

    public FileMessageRepository(@Value("${discodeit.repository.file-directory}") String directory) {
        this.filePath = directory + "/messages.json";
    }

    @Override
    public Message save(Message message) {
        List<Message> list = findAll();
        list.removeIf(e -> e.getId().equals(message.getId()));
        list.add(message);
        FileUtil.saveToFile(filePath, list);
        return message;
    }

    @Override
    public Optional<Message> findById(UUID id) {
        return findAll().stream().filter(e -> e.getId().equals(id)).findFirst();
    }

    @Override
    public List<Message> findAll() {
        return FileUtil.readListFromFile(filePath, Message.class);
    }

    @Override
    public void delete(UUID id) {
        List<Message> list = findAll();
        list.removeIf(e -> e.getId().equals(id));
        FileUtil.saveToFile(filePath, list);
    }

    @Override
    public List<Message> findAllByChannelId(UUID channelId) {
        return findAll().stream()
                .filter(m -> m.getChannelId().equals(channelId))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Message> findTopByChannelIdOrderByCreatedAtDesc(UUID channelId) {
        return findAll().stream()
                .filter(m -> m.getChannelId().equals(channelId))
                .sorted((m1, m2) -> m2.getCreatedAt().compareTo(m1.getCreatedAt()))
                .findFirst();
    }

    @Override
    public void deleteByChannelId(UUID channelId) {
        List<Message> list = findAll();
        list.removeIf(e -> e.getChannelId().equals(channelId));
        FileUtil.saveToFile(filePath, list);
    }
}