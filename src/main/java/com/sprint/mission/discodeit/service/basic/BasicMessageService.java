package com.sprint.mission.discodeit.service.basic; // 패키지명은 프로젝트 구조에 따라 다를 수 있습니다.

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository; //  Channel/User Repository import
import com.sprint.mission.discodeit.repository.UserRepository;     //
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.util.ValidationUtil;
import java.util.List;
import java.util.NoSuchElementException; // 예외 처리 추가
import java.util.Optional;
import java.util.UUID;

// MessageService 인터페이스 구현
public class BasicMessageService implements MessageService {

    // 1. Repository 필드 선언 (3가지 Repository 모두 DI 받음)
    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository; // Service 간 의존성 제거 후 추가
    private final UserRepository userRepository;       // Service 간 의존성 제거 후 추가

    // 2. 생성자를 통한 의존성 주입 (DI)
    public BasicMessageService(
            MessageRepository messageRepository,
            ChannelRepository channelRepository,
            UserRepository userRepository
    ) {
        this.messageRepository = messageRepository;
        this.channelRepository = channelRepository;
        this.userRepository = userRepository;
    }

    // --- Service 인터페이스 구현 (책임 분리 및 유효성 검사 반영) ---

    @Override
    public Message create(UUID senderId, UUID channelId, String content) {
        // 1. 유효성 검사 (Service 책임)
        if (senderId == null || channelId == null) {
            throw new IllegalArgumentException("발신자 ID와 채널 ID는 필수입니다.");
        }
        ValidationUtil.validateNotNullOrEmpty(content, "메시지 내용");

        // ✨ 2. 참조 무결성 검사 (Service 책임) - Service 대신 Repository를 통해 확인
        // 발신자 (User) 존재 여부 확인
        if (userRepository.findById(senderId).isEmpty()) {
            throw new NoSuchElementException("발신자 (User) ID " + senderId + "를 찾을 수 없습니다.");
        }
        // 채널 (Channel) 존재 여부 확인
        if (channelRepository.findById(channelId).isEmpty()) {
            throw new NoSuchElementException("채널 (Channel) ID " + channelId + "를 찾을 수 없습니다.");
        }

        // 3. Entity 객체 생성 및 저장
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

        // 3. Entity의 상태 변경 메서드 호출 (Message.java에 update(String) 메서드가 있어야 함)
        messageToUpdate.update(newContent);

        // 4. Repository에 수정된 Entity 저장
        return messageRepository.save(messageToUpdate);
    }

    // 나머지 findById, findAll, delete 메서드

    @Override
    public Optional<Message> findById(UUID id) {
        return messageRepository.findById(id);
    }

    @Override
    public List<Message> findAll() {
        return messageRepository.findAll();
    }

    @Override
    public void delete(UUID id) {
        messageRepository.delete(id);
    }
}