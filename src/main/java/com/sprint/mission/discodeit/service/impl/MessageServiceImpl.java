package com.sprint.mission.discodeit.service.impl;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.file.FileMessageRepository;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.util.ValidationUtil;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository = FileMessageRepository.getInstance();

    @Override
    public Message create(UUID senderId, UUID channelId, String content) {
        // ✨ 1. 유효성 검사 (ID는 null, content는 NotNullOrEmpty)
        if (senderId == null || channelId == null) {
            throw new IllegalArgumentException("발신자 ID와 채널 ID는 필수입니다.");
        }
        ValidationUtil.validateNotNullOrEmpty(content, "메시지 내용");

        // ✨ 2. Entity 객체 생성
        Message newMessage = new Message(senderId, channelId, content);

        // 3. Repository에 저장 요청
        return messageRepository.save(newMessage);
    }

    @Override
    public Message update(UUID messageId, String newContent) {
        // 1. 유효성 검사
        ValidationUtil.validateNotNullOrEmpty(newContent, "새 메시지 내용");

        // 2. 대상 Entity를 Repository에서 조회
        Message messageToUpdate = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("수정할 메시지를 찾을 수 없습니다: " + messageId));

        // ✨ 3. Entity의 상태 변경 메서드 호출
        messageToUpdate.update(newContent);

        // 4. Repository에 수정된 Entity 저장
        return messageRepository.save(messageToUpdate);
    }

    // 나머지 조회 및 삭제 메서드는 변경 없이 유지
    @Override
    public Optional<Message> findById(UUID id) { return messageRepository.findById(id); }

    @Override
    public List<Message> findAll() { return messageRepository.findAll(); }

    @Override
    public void delete(UUID id) { messageRepository.delete(id); }
}