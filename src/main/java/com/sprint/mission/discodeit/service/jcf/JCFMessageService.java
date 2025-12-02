package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;

import java.util.*;

public class JCFMessageService implements MessageService {

    private final Map<UUID, Message> data = new HashMap<>();

    // 유저 Service
    private final UserService userService;

    // 채널 Service
    private final ChannelService channelService;


    public JCFMessageService(UserService userService, ChannelService channelService) {
        this.userService = userService;
        this.channelService = channelService;
    }

    @Override
    public Message createMessage(UUID userId, UUID channelId, String contents) {

        // 채널이 있는가? 유저가 있는가? 를 먼저 체크한다.
        // 1. 유저가 존재하나?
        User user = userService.findUser(userId);

        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다. userId = " + userId);
        }

        // 2. 채널이 존재하나?
        Channel channel = channelService.findChannel(channelId);

        if (channel == null) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다. channelId = " + channelId);
        }

        // 3. 검증 완료
        Message message = new Message(userId, channelId, contents);
        data.put(message.getId(), message);
        return message;
    }

    @Override
    public Message findMessage(UUID id) {
        return data.get(id);
    }

    @Override
    public List<Message> findAllMessages() {
        return new ArrayList<>(data.values());
    }

    @Override
    public Message updateMessage(UUID id, String contents) {
        Message message = data.get(id);

        if (message == null) {
            return null;
        }
        message.update(contents);
        return message;

    }

    @Override
    public void deleteMessage(UUID id) {
        data.remove(id);
    }
}
