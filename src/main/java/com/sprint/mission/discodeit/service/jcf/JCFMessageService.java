package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.ChannelService;
import java.util.*;
import java.util.stream.Collectors;

public class JCFMessageService implements MessageService {

    // 심화 요구사항: 관계 검증을 위한 의존성 필드
    private final UserService userService;
    private final ChannelService channelService;

    // 싱글톤 패턴 구현
    private static JCFMessageService INSTANCE;

    // JCF 필드
    private final Map<UUID, Message> data;

    // private 생성자: 의존성 주입을 받도록 설계
    private JCFMessageService(UserService userService, ChannelService channelService) {
        this.data = new HashMap<>();
        this.userService = userService;
        this.channelService = channelService;
    }

    // 팩토리 메서드: 의존성을 받아서 인스턴스를 반환
    public static JCFMessageService getInstance(UserService userService, ChannelService channelService) {
        if (INSTANCE == null) {
            // Lazy Initialization (지연 초기화)
            INSTANCE = new JCFMessageService(userService, channelService);
        }
        return INSTANCE;
    }

    // --- CRUD 구현 ---

    @Override
    public Message save(Message message) {
        // 심화 요구사항: Message 생성 전 연관 도메인 모델 검증
        if (userService.findById(message.getUserId()).isEmpty()) {
            throw new IllegalArgumentException("메시지 작성자(User ID: " + message.getUserId() + ")가 존재하지 않아 메시지를 저장할 수 없습니다.");
        }
        if (channelService.findById(message.getChannelId()).isEmpty()) {
            throw new IllegalArgumentException("메시지가 속한 Channel ID: (" + message.getChannelId() + ")이(가) 존재하지 않아 메시지를 저장할 수 없습니다.");
        }

        // 검증 통과 시 저장
        data.put(message.getId(), message);
        return message;
    }

    @Override
    public Optional<Message> findById(UUID id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public List<Message> findAll() {
        return data.values().stream().collect(Collectors.toList());
    }

    @Override
    public Message update(Message message) {
        if (data.containsKey(message.getId())) {
            data.put(message.getId(), message);
            return message;
        }
        throw new NoSuchElementException("수정할 Message ID가 존재하지 않습니다: " + message.getId());
    }

    @Override
    public void delete(UUID id) {
        data.remove(id);
    }
}