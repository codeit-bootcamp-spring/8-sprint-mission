package com.sprint.mission.discodeit.service.jcf;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;

import java.util.*;
import java.util.stream.Collectors;

public class JCFMessageService implements MessageService {
    private final List<Message> data;
    private final MessageRepository messageRepository;
    private final UserService userService;
    private final ChannelService channelService;

    public JCFMessageService(MessageRepository messageRepository, UserService userService, ChannelService channelService) {
        this.data = new ArrayList<>();
        this.messageRepository = messageRepository;
        this.userService = userService;
        this.channelService = channelService;
    }


    @Override
    public void createMessage(Message message) {
        message.setModifiedAt(message.getCreatedAt());
        data.add(message);
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
        Message message = new Message(uid, cid, channel.getName(), user.getName(), content);
        message.setModifiedAt(message.getCreatedAt());
        data.add(message);
        return message;
    }

    @Override
    public Message findById(UUID id) {
        return data.stream()
                .filter(message -> message.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Message> findMessages() {
        return data;
    }

    @Override
    public Map<String, List<Message>> findMessagesByFrom() {
        return data.stream()
                .collect(
                        Collectors.groupingBy(
                                Message::getFrom,
                                Collectors.toList()
                        ));
    }

    @Override
    public void updateMessage(UUID id, String fieldName, String value) {
        data.stream()
                .filter( message -> message.getId().equals(id))
                .forEach(message -> {
                    if(fieldName.equals("channelName")) {
                      message.setChannelName(value);
                      message.setModifiedAt(System.currentTimeMillis());
                    } else if(fieldName.equals("from")) {
                        message.setFrom(value);
                        message.setModifiedAt(System.currentTimeMillis());
                    } else if (fieldName.equals("content")) {
                        message.setContent(value);
                        message.setModifiedAt(System.currentTimeMillis());
                    }
                });
    }

    @Override
    public void deleteMessage(UUID id) {
        Iterator<Message> iterator = data.iterator();
        while(iterator.hasNext()) {
            Message message = iterator.next();
            if(message.getId().equals(id)) {
                iterator.remove();
                break;
            }
        }
    }

    @Override
    public Optional<Message> findByMId(UUID id) {
        return Optional.empty();
    }

    @Override
    public void saveMessage(Message message) {
        message.setModifiedAt(message.getCreatedAt());
        data.add(messageRepository.save(message));
    }


}
