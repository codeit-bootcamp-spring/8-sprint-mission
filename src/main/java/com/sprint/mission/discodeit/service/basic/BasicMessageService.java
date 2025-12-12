package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BasicMessageService implements MessageService {

    // 유저 Service
    private final UserService userService;

    // 채널 Service
    private final ChannelService channelService;

    // 메시지 Repository
    private final MessageRepository messageRepository;

    // AppConfig에서 주입 (DI)
    public BasicMessageService(UserService userService, ChannelService channelService, MessageRepository messageRepository) {
        this.userService = userService;
        this.channelService = channelService;
        this.messageRepository = messageRepository;
    }

    @Override
    public Message createMessage(UUID userId, UUID channelId, String contents) {

        // 유저가 존재하나?
        User user = userService.findUser(userId);

        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다. userId = " + userId);
        }

        // 채널이 존재하나?
        Channel channel = channelService.findChannel(channelId);

        if (channel == null) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다. channelId = " + channelId);
        }

        // 검증 완료
        Message message = new Message(userId, channelId, contents);

        return messageRepository.save(message);
    }

    @Override
    public Message findMessage(UUID id) {
        return messageRepository.findById(id);
    }

    @Override
    public List<Message> findAllMessages() {
        return messageRepository.findAll();
    }

    @Override
    public Message updateMessage(UUID id, String contents) {
        // 기존 메시지 조회
        Message message = findMessage(id);

        if (message == null) throw new IllegalArgumentException("해당 메시지가 존재하지 않습니다." + id);

        message.update(contents);

        return messageRepository.update(id, message);
    }

    @Override
    public void deleteMessage(UUID id) {
        messageRepository.delete(id);
    }
}
