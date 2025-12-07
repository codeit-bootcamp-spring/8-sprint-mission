// BasicMessageService는 User/Channel 검증 비즈니스 로직을 수행합니다.
package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.service.MessageService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BasicMessageService implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;

    // 생성자를 통한 의존성 주입
    public BasicMessageService(MessageRepository messageRepository, UserRepository userRepository, ChannelRepository channelRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.channelRepository = channelRepository;
    }

    @Override
    public Message save(Message message) {
        // **비즈니스 로직**: User/Channel 존재 검증
        if (userRepository.findById(message.getUserId()).isEmpty()) {
            throw new IllegalArgumentException("메시지 작성자(User ID: " + message.getUserId() + ")가 존재하지 않습니다.");
        }
        if (channelRepository.findById(message.getChannelId()).isEmpty()) {
            throw new IllegalArgumentException("메시지가 속한 Channel ID: (" + message.getChannelId() + ")이(가) 존재하지 않습니다.");
        }

        // **저장 로직**: Repository에 위임
        return messageRepository.save(message);
    }

    // ... 나머지 findById, findAll, update, delete는 모두 messageRepository에 위임
    @Override
    public Optional<Message> findById(UUID id) { return messageRepository.findById(id); }
    @Override
    public List<Message> findAll() { return messageRepository.findAll(); }
    @Override
    public Message update(Message message) { return messageRepository.save(message); }
    @Override
    public void delete(UUID id) { messageRepository.delete(id); }
}