package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor // 3개의 final 레포지토리를 모두 주입하는 생성자를 만듭니다.
public class BasicMessageService implements MessageService {

    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;

    @Override
    public Message create(UUID authorId, UUID channelId, String content) {
        userRepository.findById(authorId).orElseThrow(() -> new IllegalArgumentException("Author not found"));
        channelRepository.findById(channelId).orElseThrow(() -> new IllegalArgumentException("Channel not found"));

        Message message = new Message(authorId, channelId, content);
        return messageRepository.save(message);
    }

    @Override
    public Optional<Message> findById(UUID id) {
        return messageRepository.findById(id);
    }

    @Override
    public List<Message> findAll() {
        return messageRepository.findAll();
    }

    @Override
    public Message update(UUID id, String content) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Message not found: " + id));
        message.update(content);
        return messageRepository.save(message);
    }

    @Override
    public void delete(UUID id) {
        messageRepository.delete(id);
    }
}