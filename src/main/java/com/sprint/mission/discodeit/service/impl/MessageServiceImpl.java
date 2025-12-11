package com.sprint.mission.discodeit.service.impl;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository; //  추가/변경
import com.sprint.mission.discodeit.repository.UserRepository;     //  추가/변경
import com.sprint.mission.discodeit.repository.file.FileMessageRepository;
import com.sprint.mission.discodeit.repository.file.FileChannelRepository; //  추가/변경 (초기화용)
import com.sprint.mission.discodeit.repository.file.FileUserRepository;   //  추가/변경 (초기화용)
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.util.ValidationUtil;
import java.util.List;
import java.util.NoSuchElementException; // 예외 처리 추가
import java.util.Optional;
import java.util.UUID;

public class MessageServiceImpl implements MessageService {

    //  1. 의존성 주입 대상 변경: Service 대신 Repository에 의존
    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;

    //  2. 생성자를 통한 의존성 주입 (DI)
    public MessageServiceImpl(
            MessageRepository messageRepository,
            ChannelRepository channelRepository,
            UserRepository userRepository
    ) {
        this.messageRepository = messageRepository;
        this.channelRepository = channelRepository;
        this.userRepository = userRepository;
    }

    // (선택 사항: 기본 생성자 제거 또는 DI를 사용하지 않는 경우를 위한 기본값 설정)
    public MessageServiceImpl() {
        // DI가 아닌 경우, File 구현체를 직접 사용하도록 초기화
        this.messageRepository = FileMessageRepository.getInstance();
        this.channelRepository = FileChannelRepository.getInstance();
        this.userRepository = FileUserRepository.getInstance();
    }

    @Override
    public Message create(UUID senderId, UUID channelId, String content) {
        // 1. 유효성 검사
        if (senderId == null || channelId == null) {
            throw new IllegalArgumentException("발신자 ID와 채널 ID는 필수입니다.");
        }
        ValidationUtil.validateNotNullOrEmpty(content, "메시지 내용");

        //  3. Service 대신 Repository를 통해 엔티티의 존재 유무 확인 (심화 요구사항 반영)
        // 존재하지 않는 ID로 메시지를 생성하는 것을 방지
        if (userRepository.findById(senderId).isEmpty()) {
            throw new NoSuchElementException("발신자 (User) ID " + senderId + "를 찾을 수 없습니다.");
        }
        if (channelRepository.findById(channelId).isEmpty()) {
            throw new NoSuchElementException("채널 (Channel) ID " + channelId + "를 찾을 수 없습니다.");
        }

        // 4. Entity 객체 생성 및 저장
        Message newMessage = new Message(senderId, channelId, content);
        return messageRepository.save(newMessage);
    }

    @Override
    public Message update(UUID messageId, String newContent) {
        // 1. 유효성 검사
        ValidationUtil.validateNotNullOrEmpty(newContent, "새 메시지 내용");

        // 2. 대상 Entity를 Repository에서 조회
        Message messageToUpdate = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("수정할 메시지를 찾을 수 없습니다: " + messageId));

        // 3. Entity의 상태 변경 메서드 호출
        messageToUpdate.update(newContent);

        // 4. Repository에 수정된 Entity 저장
        return messageRepository.save(messageToUpdate);
    }

    // 나머지 findById, findAll, delete 메서드는 messageRepository를 호출하도록 유지
    @Override
    public Optional<Message> findById(UUID id) { return messageRepository.findById(id); }

    @Override
    public List<Message> findAll() { return messageRepository.findAll(); }

    @Override
    public void delete(UUID id) { messageRepository.delete(id); }
}