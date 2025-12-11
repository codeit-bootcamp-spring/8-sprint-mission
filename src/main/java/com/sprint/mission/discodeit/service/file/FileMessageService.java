package com.sprint.mission.discodeit.service.file; // 패키지명은 프로젝트 구조에 따라 다를 수 있습니다.

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository; // MessageRepository import
import com.sprint.mission.discodeit.repository.ChannelRepository; // MessageService DI에 필요
import com.sprint.mission.discodeit.repository.UserRepository;     // MessageService DI에 필요
import com.sprint.mission.discodeit.repository.file.FileMessageRepository; // 기본 초기화용 import
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.util.ValidationUtil;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

public class FileMessageService implements MessageService {

    // 1. Repository 필드 선언 (3가지 Repository 모두 DI 받음)
    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;

    // 2. Repository를 주입받는 생성자 (DI)
    public FileMessageService(
            MessageRepository messageRepository,
            ChannelRepository channelRepository,
            UserRepository userRepository
    ) {
        this.messageRepository = messageRepository;
        this.channelRepository = channelRepository;
        this.userRepository = userRepository;
    }

    // 3. (선택적) DI를 사용하지 않을 경우를 위한 기본 생성자
    public FileMessageService() {
        // 실제 구현체를 직접 인스턴스화
        this.messageRepository = FileMessageRepository.getInstance();
        // Channel/User Repository도 파일 구현체로 초기화 필요
        this.channelRepository = com.sprint.mission.discodeit.repository.file.FileChannelRepository.getInstance();
        this.userRepository = com.sprint.mission.discodeit.repository.file.FileUserRepository.getInstance();
    }

    // --- Service 인터페이스 구현 (update 메서드 추가) ---

    @Override
    public Message create(UUID senderId, UUID channelId, String content) {
        // 1. 유효성 검사
        if (senderId == null || channelId == null) {
            throw new IllegalArgumentException("발신자 ID와 채널 ID는 필수입니다.");
        }
        ValidationUtil.validateNotNullOrEmpty(content, "메시지 내용");

        // 2. 참조 무결성 검사 (Repository를 통해 확인)
        if (userRepository.findById(senderId).isEmpty()) {
            throw new NoSuchElementException("발신자 (User) ID " + senderId + "를 찾을 수 없습니다.");
        }
        if (channelRepository.findById(channelId).isEmpty()) {
            throw new NoSuchElementException("채널 (Channel) ID " + channelId + "를 찾을 수 없습니다.");
        }

        // 3. Entity 객체 생성 및 저장
        Message newMessage = new Message(senderId, channelId, content);
        return messageRepository.save(newMessage);
    }

    @Override
    public Message update(UUID messageId, String newContent) {
        //  1. 유효성 검사
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