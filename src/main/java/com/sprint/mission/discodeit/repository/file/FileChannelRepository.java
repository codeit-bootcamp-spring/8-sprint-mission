package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FileChannelRepository implements ChannelRepository {

    private static final String DATA_DIR = "data";
    private static final String CHANNEL_DIR = "channel";
    private static final String DATA_FILE = DATA_DIR + File.separator + CHANNEL_DIR + File.separator + "channels.ser";

    @Override
    public Channel save(Channel channel) {


        // 파일에서 기존 Channel 목록 읽기 (역직렬화)
        List<Channel> channels = loadChannelsFromFile();

        // 기존에 같은 ID 있으면 제거 후 새로 넣기
        channels.removeIf(c -> c.getId().equals(channel.getId()));
        channels.add(channel);

        // 전체 목록을 다시 파일에 저장 (직렬화)
        saveChannelsToFile(channels);

        return channel;
    }

    @Override
    public Optional<Channel> findById(UUID id) {
        return Optional.ofNullable(loadChannelsFromFile()
                .stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null));
    }

    @Override
    public List<Channel> findAll() {
        return new ArrayList<>(loadChannelsFromFile());
    }

    @Override
    public boolean existsById(UUID id) {
        return false;
    }

    @Override
    public void delete(UUID id) {
        List<Channel> channels = loadChannelsFromFile();
        channels.removeIf(c -> c.getId().equals(id));
        saveChannelsToFile(channels);
    }

    private File getDataFile() {

        File dir = new File(DATA_DIR + File.separator + CHANNEL_DIR);

        if (!dir.exists()) {
            dir.mkdirs();  // data/channel 디렉터리 없으면 생성
        }

        return new File(DATA_FILE);
    }

    private List<Channel> loadChannelsFromFile() {

        File file = getDataFile();

        // 파일이 없으면 빈 리스트 반환
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<Channel>) ois.readObject();
        } catch (Exception e) {
            // 역직렬화 실패 시 빈 리스트
            return new ArrayList<>();
        }
    }

    private void saveChannelsToFile(List<Channel> channels) {
        File file = getDataFile();

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(channels);
        } catch (Exception e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }
}
