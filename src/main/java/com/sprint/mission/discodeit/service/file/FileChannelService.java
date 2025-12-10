package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.UserService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class FileChannelService implements ChannelService {
    private final ChannelRepository channelRepository;
    private final UserService userService;
    private final Path directory = Paths.get(System.getProperty("user.dir"), "data", "channels");

    public FileChannelService(ChannelRepository channelRepository, UserService userService) {
        this.channelRepository = channelRepository;
        this.userService = userService;
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

    public static <T> void save(Path filePath, T data) {
        try (
                FileOutputStream fos = new FileOutputStream(filePath.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos);
        ) {
            oos.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> load(Path directory) {
        if (Files.exists(directory)) {
            try {
                List<T> list = Files.list(directory)
                        .map(path -> {
                            try (
                                    FileInputStream fis = new FileInputStream(path.toFile());
                                    ObjectInputStream ois = new ObjectInputStream(fis);
                            ) {
                               Object data = ois.readObject();
                               return (T) data;
                            } catch (IOException | ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .toList();
                return list;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public void createChannel(Channel channel) {
        channel.setModifiedAt(channel.getCreatedAt());
        Path filePath = directory.resolve(channel.getFileName());
        save(filePath, channel);
    }

    @Override
    public Channel create(UUID uid, String name, String host, int participant, List<String> participants) {
        User user = userService.findById(uid);
        if(user == null) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }
        return new Channel(uid, name, user.getName(), participant, participants);
    }

    @Override
    public Channel findById(UUID id) {
        List<Channel> channel = load(directory);
        return channel.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Channel> findChannels() {
        return load(directory);
    }

    @Override
    public Map<String, List<Channel>> findChannelByName() {
        List<Channel> channels = load(directory);
        return channels.stream()
                .collect(
                        Collectors.groupingBy(
                                Channel::getName,
                                Collectors.toList()
                        ));
    }

    @Override
    public List<Channel> findChannelByTopNParticipant(int n) {
        List<Channel> channels = load(directory);
        return channels.stream()
                .sorted((c1, c2) -> c2.getParticipant() - c1.getParticipant())
                .limit(n)
                .collect(Collectors.toList());

    }

    @Override
    public List<String> findChannelByParticipantsASC(UUID id) {
        List<Channel> channels = load(directory);
        return channels.stream()
                .filter(channel -> channel.getId().equals(id))
                .findFirst()
                .map(Channel::getParticipants)
                .map(participants -> {
                    List<String> sortedList = new ArrayList<>(participants);
                    Collections.sort(sortedList);
                    return sortedList;
                })
                .orElse(Collections.emptyList());
    }

    @Override
    public void updateChannel(UUID id, String fieldName, String value) {
        List<Channel> channels = load(directory);
        Optional<Channel> optionalChannel = channels.stream()
                .filter(channel -> channel.getId().equals(id))
                .findFirst();

        if(optionalChannel.isPresent()) {
            Channel channel = optionalChannel.get();

            if(fieldName.equals("name")) {
                channel.setName((String) value);
            } else if (fieldName.equals("host")) {
                channel.setHost((String) value);
            } else if (fieldName.equals("participant")) {
                channel.setParticipant(Integer.parseInt(value));
            } else if (fieldName.equals("participants")) {
                List<String> currentParticipants = channel.getParticipants();
                List<String> newParticipants = new ArrayList<>(currentParticipants);
                newParticipants.add(value);
                channel.setParticipants(newParticipants);
            }

            channel.setModifiedAt(System.currentTimeMillis());

            Path filePath = directory.resolve(channel.getFileName());
            save(filePath, channel);
        }
    }

    @Override
    public void deleteChannel(UUID id) {
        List<Channel> channels = load(directory);

        Optional<Channel> optionalChannel = channels.stream()
                .filter(channel -> channel.getId().equals(id))
                .findFirst();
        if (optionalChannel.isPresent()) {
            Channel channel = optionalChannel.get();
            Path filePath = directory.resolve(channel.getFileName());

            try {
                Files.deleteIfExists(filePath);
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Optional<Channel> findByCId(UUID id) {
        return channelRepository.findById(id);
    }

    @Override
    public void saveChannel(Channel channel) {
        if(findByCId(channel.getId()).isEmpty()) {
            channelRepository.save(channel);
        } else {
            updateChannel(channel.getId(), "participant", "2");
            updateChannel(channel.getId(), "participants", "이재준");
        }

    }
}
