package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class FileChannelRepository implements ChannelRepository {

    private static FileChannelRepository INSTANCE;
    private static final String FILE_PATH = "data/channel.json";

    private FileChannelRepository() {}

    public static FileChannelRepository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FileChannelRepository();
        }
        return INSTANCE;
    }

    // --- 파일 IO 유틸리티 ---
    private Map<UUID, Channel> readAll() {
        File file = new File(FILE_PATH);
        if (!file.exists() || file.length() == 0) {
            return new HashMap<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Map<UUID, Channel>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Channel Repository 역직렬화 오류: " + e.getMessage());
            return new HashMap<>();
        }
    }

    private void writeAll(Map<UUID, Channel> data) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(data);
        } catch (IOException e) {
            System.err.println("Channel Repository 직렬화 오류: " + e.getMessage());
        }
    }

    // --- Repository 인터페이스 구현 ---

    @Override
    public Channel save(Channel channel) {
        Map<UUID, Channel> data = readAll();
        data.put(channel.getId(), channel);
        writeAll(data);
        return channel;
    }

    @Override
    public Optional<Channel> findById(UUID id) {
        return Optional.ofNullable(readAll().get(id));
    }

    @Override
    public List<Channel> findAll() {
        return readAll().values().stream().collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        Map<UUID, Channel> data = readAll();
        data.remove(id);
        writeAll(data);
    }
}