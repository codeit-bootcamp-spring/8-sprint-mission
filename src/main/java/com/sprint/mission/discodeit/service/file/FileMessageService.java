package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class FileMessageService implements MessageService {
    private final MessageRepository messageRepository;
    private final UserService userService;
    private final ChannelService channelService;
    private final Path directory = Paths.get(System.getProperty("user.dir"), "data", "messages");

    public FileMessageService(MessageRepository messageRepository, UserService userService, ChannelService channelService) {
        this.messageRepository = messageRepository;
        this.userService = userService;
        this.channelService = channelService;
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
    public void createMessage(Message message) {
        message.setModifiedAt(message.getCreatedAt());
        Path filePath = directory.resolve(message.getFileName());
        save(filePath, message);
    }

    @Override
    public Message create(UUID uid, UUID cid, String channelName, String from, String content) {
        User user = userService.findById(uid);
        if(user == null) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }
        Channel channel = channelService.findById(cid);
        if(channel == null) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
        }
        return new Message(uid, cid, channel.getName(), user.getName(), content);
    }

    @Override
    public Message findById(UUID id) {
        List<Message> message = load(directory);
        return message.stream()
                .filter(m -> m.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Message> findMessages() {
        return load(directory);
    }

    @Override
    public Map<String, List<Message>> findMessagesByFrom() {
        List<Message> messages = load(directory);
        return messages.stream()
                .collect(
                        Collectors.groupingBy(
                                Message::getFrom,
                                Collectors.toList()
                        ));
    }

    @Override
    public void updateMessage(UUID id, String fieldName, String value) {
        List<Message> messages = load(directory);
        Optional<Message> optionalMessage = messages.stream()
                .filter(m -> m.getId().equals(id))
                .findFirst();
        if (optionalMessage.isPresent()) {
            Message message = optionalMessage.get();

            if(fieldName.equals("channelName")) {
                message.setChannelName(value);
            } else if(fieldName.equals("from")) {
                message.setFrom(value);
            } else if(fieldName.equals("content")) {
                message.setContent(value);
            }

            message.setModifiedAt(System.currentTimeMillis());
            Path filePath = directory.resolve(message.getFileName());
            save(filePath, message);
        }
    }

    @Override
    public void deleteMessage(UUID id) {
        List<Message> messages = load(directory);

        Optional<Message> optionalMessage = messages.stream()
                .filter(message -> message.getId().equals(id))
                .findFirst();
        if (optionalMessage.isPresent()) {
            Message message = optionalMessage.get();
            Path filePath = directory.resolve(message.getFileName());

            try {
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public Optional<Message> findByMId(UUID id) {
        return messageRepository.findById(id);
    }

    @Override
    public void saveMessage(Message message) {
        if(findByMId(message.getId()).isEmpty()) {
            messageRepository.save(message);
        } else {
            updateMessage(message.getId(), "content", "행복해요.");
        }

    }
}
