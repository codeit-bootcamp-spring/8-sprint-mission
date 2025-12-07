package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.service.ChannelService;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class FileChannelService implements ChannelService {

    private static FileChannelService INSTANCE;
    private static final String FILE_PATH = "channels.dat";

    private FileChannelService() {
        // 싱글톤 패턴
    }

    public static FileChannelService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FileChannelService();
        }
        return INSTANCE;
    }

    // --- 파일 IO 유틸리티 (역직렬화) ---
    private Map<UUID, Channel> readAll() {
        File file = new File(FILE_PATH);
        if (!file.exists() || file.length() == 0) {
            return new HashMap<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Map<UUID, Channel>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Channel 데이터 역직렬화 오류: " + e.getMessage());
            return new HashMap<>();
        }
    }

    // --- 파일 IO 유틸리티 (직렬화) ---
    private void writeAll(Map<UUID, Channel> data) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(data);
        } catch (IOException e) {
            System.err.println("Channel 데이터 직렬화 오류: " + e.getMessage());
        }
    }

    // --- ChannelService 인터페이스 구현 (CRUD) ---

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
    public Channel update(Channel channel) {
        Map<UUID, Channel> data = readAll();
        if (data.containsKey(channel.getId())) {
            data.put(channel.getId(), channel);
            writeAll(data);
            return channel;
        }
        throw new NoSuchElementException("수정할 Channel ID가 존재하지 않습니다: " + channel.getId());
    }

    @Override
    public void delete(UUID id) {
        Map<UUID, Channel> data = readAll();
        data.remove(id);
        writeAll(data);
    }
}