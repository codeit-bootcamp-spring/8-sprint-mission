package com.sprint.mission.discodeit.service.jcf; // 패키지명은 프로젝트 구조에 따라 다를 수 있습니다.

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.ChannelService; // DI된 Service import
import com.sprint.mission.discodeit.service.UserService;     // DI된 Service import
import com.sprint.mission.discodeit.util.ValidationUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.NoSuchElementException; // 예외 처리용 import

// MessageService 인터페이스 구현
public class JCFMessageService implements MessageService {

    // 1. JCF (Map) 필드 선언 (메모리 저장소)
    private final Map<UUID, Message> data;

    // 2. 서비스 간 의존성 주입 필드 (모범 답안 반영)
    private final ChannelService channelService;
    private final UserService userService;

    // 3. 생성자를 통한 의존성 주입 (DI)
    public JCFMessageService(ChannelService channelService, UserService userService) {
        this.data = new HashMap<>();
        this.channelService = channelService;
        this.userService = userService;
    }

    // --- Service 인터페이스 구현 (update 메서드 추가) ---

    @Override
    public Message create(UUID senderId, UUID channelId, String content) {
        // 1. 유효성 검사
        if (senderId == null || channelId == null) {
            throw new IllegalArgumentException("발신자 ID와 채널 ID는 필수입니다.");
        }
        ValidationUtil.validateNotNullOrEmpty(content, "메시지 내용");

        // 2. 참조 무결성 검사 (DI된 Service를 통해 확인)
        // Service가 아닌 Repository를 통해 확인하도록 변경하는 것이 2차 미션의 목표입니다.
        // JCFService는 이전 단계의 구현체라 Service를 통해 확인합니다.
        try {
            channelService.findById(channelId).orElseThrow(() -> new NoSuchElementException("Channel ID를 찾을 수 없습니다."));
            userService.findById(senderId).orElseThrow(() -> new NoSuchElementException("User ID를 찾을 수 없습니다."));
        } catch (NoSuchElementException e) {
            throw e;
        }

        // 3. Entity 객체 생성 및 저장
        Message newMessage = new Message(senderId, channelId, content);
        data.put(newMessage.getId(), newMessage);
        return newMessage;
    }

    @Override
    public Message update(UUID messageId, String newContent) {
        //  1. 유효성 검사
        ValidationUtil.validateNotNullOrEmpty(newContent, "새 메시지 내용");

        // 2. 대상 Entity를 JCF Map에서 조회
        Message messageToUpdate = Optional.ofNullable(data.get(messageId))
                .orElseThrow(() -> new NoSuchElementException("수정할 메시지 ID를 찾을 수 없습니다: " + messageId));

        // 3. Entity의 상태 변경 메서드 호출 (Message.java에 update(String) 메서드가 있어야 함)
        messageToUpdate.update(newContent);

        // 4. JCF Map에 다시 저장 (참조형이므로 사실상 Map의 객체가 직접 수정됨)
        return messageToUpdate;
    }

    // 나머지 findById, findAll, delete 메서드

    @Override
    public Optional<Message> findById(UUID id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public List<Message> findAll() {
        return data.values().stream().collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        if (!data.containsKey(id)) {
            throw new NoSuchElementException("삭제할 메시지 ID를 찾을 수 없습니다: " + id);
        }
        data.remove(id);
    }
}