package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.util.FileUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileChannelRepository implements ChannelRepository {
    private final String filePath;

    public FileChannelRepository(@Value("${discodeit.repository.file-directory}") String directory) {
        this.filePath = directory + "/channels.json";
    }

    @Override
    public Channel save(Channel channel) {
        List<Channel> list = findAll();
        list.removeIf(e -> e.getId().equals(channel.getId()));
        list.add(channel);
        FileUtil.saveToFile(filePath, list);
        return channel;
    }

    @Override
    public Optional<Channel> findById(UUID id) {
        return findAll().stream().filter(e -> e.getId().equals(id)).findFirst();
    }

    @Override
    public List<Channel> findAll() {
        return FileUtil.readListFromFile(filePath, Channel.class);
    }

    @Override
    public void delete(UUID id) {
        List<Channel> list = findAll();
        list.removeIf(e -> e.getId().equals(id));
        FileUtil.saveToFile(filePath, list);
    }
}