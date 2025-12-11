package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.file.FileChannelRepository;
import com.sprint.mission.discodeit.repository.file.FileMessageRepository;
import com.sprint.mission.discodeit.repository.file.FileUserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.impl.ChannelServiceImpl;
import com.sprint.mission.discodeit.service.impl.MessageServiceImpl;
import com.sprint.mission.discodeit.service.impl.UserServiceImpl;
import com.sprint.mission.discodeit.util.ValidationUtil; // 유효성 검사 테스트를 위해 사용

import java.util.List;
import java.util.UUID;

public class JavaApplication {

    // --- 1. 셋업 및 테스트 유틸리티 메서드 ---

    static User setupUser(UserService userService) {
        // Service의 create 메서드를 사용하여 Entity 생성 책임 위임
        User user = userService.create("Alice_Basic", "alice@basic.com");
        System.out.println("SETUP User 생성: " + user.getName() + " (" + user.getId() + ")");
        return user;
    }

    static Channel setupChannel(ChannelService channelService, UUID ownerId) {
        // Service의 create 메서드를 사용하여 Entity 생성 책임 위임
        Channel channel = channelService.create("Public Notice", ownerId);
        System.out.println("SETUP Channel 생성: " + channel.getName() + " (" + channel.getId() + ")");
        return channel;
    }

    static void userCRUDTest(UserService userService) {
        System.out.println("\n--- [User CRUD 테스트 시작] ---");

        // 생성 (Service 책임)
        User userA = userService.create("Alice_Test", "alice.t@codeit.com");
        System.out.println("User 생성: " + userA.getName());

        // 조회
        User foundUser = userService.findById(userA.getId()).orElse(null);
        System.out.println("User 조회: " + foundUser.getName());

        // 수정 (Service 책임)
        User updatedUser = userService.update(userA.getId(), "Alice_Updated", "alice.up@codeit.com");
        System.out.println("User 수정 후 이름: " + updatedUser.getName());

        // 삭제
        userService.delete(userA.getId());
        List<User> userList = userService.findAll();
        System.out.println("User 삭제 후 전체 수: " + userList.size());
    }

    static void messageCreateTest(MessageService messageService, Channel channel, User author) {
        System.out.println("\n--- [Message 생성 테스트 시작] ---");
        try {
            // 메시지 생성 (Service 책임) - DI된 Repository를 통해 Channel/User 존재 확인
            Message message = messageService.create(author.getId(), channel.getId(), "안녕하세요. DI 테스트 메시지입니다.");
            System.out.println("메시지 생성 성공: " + message.getContent());
        } catch (Exception e) {
            System.err.println("메시지 생성 실패: " + e.getMessage());
        }
    }

    static void validationTest(UserService userService) {
        System.out.println("\n--- [유효성 검사 테스트 시작] ---");
        try {
            // 빈 문자열 테스트 (ValidationUtil에서 예외 발생 예상)
            userService.create("", "invalid@test.com");
        } catch (IllegalArgumentException e) {
            System.out.println("유효성 검사 성공 (예외 발생): " + e.getMessage());
        }
    }

    // --- 2. Main 메서드 (DI 초기화 영역) ---

    public static void main(String[] args) {
        System.out.println("--- DISCORD CLONE APPLICATION START ---");

        // 1. 레포지토리 초기화 (File 구현체 사용)
        // Service의 의존성 주입을 위해 필요한 객체들
        UserRepository userRepository = FileUserRepository.getInstance();
        ChannelRepository channelRepository = FileChannelRepository.getInstance();
        MessageRepository messageRepository = FileMessageRepository.getInstance();

        // 2. 서비스 초기화 (Repository를 주입받아 DI 적용)
        // UserService와 ChannelService도 Repository에만 의존한다고 가정
        UserService userService = new UserServiceImpl(userRepository);
        ChannelService channelService = new ChannelServiceImpl(channelRepository);

        // MessageService는 3개의 Repository에 의존성 주입 (가장 복잡한 DI)
        MessageService messageService = new MessageServiceImpl(
                messageRepository,
                channelRepository,
                userRepository // Channel/User의 존재 확인을 위해 Repository 필요
        );

        // --- 3. 테스트 실행 ---

        // 셋업: 테스트를 위한 User와 Channel 생성 (저장소에 실제로 저장됨)
        User owner = setupUser(userService);
        Channel noticeChannel = setupChannel(channelService, owner.getId());

        // 주요 테스트
        userCRUDTest(userService);
        messageCreateTest(messageService, noticeChannel, owner);
        validationTest(userService);

        System.out.println("--- DISCORD CLONE APPLICATION END ---");
    }
}