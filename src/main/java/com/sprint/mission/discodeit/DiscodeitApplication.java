package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;
import java.util.UUID;

@SpringBootApplication
public class DiscodeitApplication {

    public static void main(String[] args) {
        // 1. Spring Boot 실행 및 Context 획득
        ConfigurableApplicationContext context = SpringApplication.run(DiscodeitApplication.class, args);

        // 2. Spring Context를 활용한 서비스 초기화 (Bean 조회)
        UserService userService = context.getBean(UserService.class);
        ChannelService channelService = context.getBean(ChannelService.class);
        MessageService messageService = context.getBean(MessageService.class);

        System.out.println("\n--- DISCORD CLONE APPLICATION START (Spring Boot) ---");

        // 3. 셋업 및 테스트 실행 (기존 JavaApplication 로직 복사)
        User owner = setupUser(userService);
        // setupChannel 메서드에 ownerId 인자를 넘기도록 기존 로직에 맞게 조정
        Channel noticeChannel = setupChannel(channelService, owner.getId());

        // 주요 테스트 시나리오 실행
        userCRUDTest(userService);
        messageCreateTest(messageService, noticeChannel, owner);
        validationTest(userService);

        System.out.println("\n--- DISCORD CLONE APPLICATION END ---");
    }

    // --- JavaApplication에서 복사해온 테스트용 정적 메소드들 ---

    static User setupUser(UserService userService) {
        User user = userService.create("Alice_Basic", "alice@basic.com");
        System.out.println("SETUP User 생성: " + user.getName() + " (" + user.getId() + ")");
        return user;
    }

    static Channel setupChannel(ChannelService channelService, UUID ownerId) {
        Channel channel = channelService.create("Public Notice", ownerId);
        System.out.println("SETUP Channel 생성: " + channel.getName() + " (" + channel.getId() + ")");
        return channel;
    }

    static void userCRUDTest(UserService userService) {
        System.out.println("\n--- [User CRUD 테스트 시작] ---");
        User userA = userService.create("Alice_Test", "alice.t@codeit.com");
        System.out.println("User 생성: " + userA.getName());

        User foundUser = userService.findById(userA.getId()).orElse(null);
        System.out.println("User 조회: " + foundUser.getName());

        User updatedUser = userService.update(userA.getId(), "Alice_Updated", "alice.up@codeit.com");
        System.out.println("User 수정 후 이름: " + updatedUser.getName());

        userService.delete(userA.getId());
        List<User> userList = userService.findAll();
        System.out.println("User 삭제 후 전체 수: " + userList.size());
    }

    static void messageCreateTest(MessageService messageService, Channel channel, User author) {
        System.out.println("\n--- [Message 생성 테스트 시작] ---");
        try {
            Message message = messageService.create(author.getId(), channel.getId(), "안녕하세요. Spring Bean 테스트 메시지입니다.");
            System.out.println("메시지 생성 성공: " + message.getContent());
        } catch (Exception e) {
            System.err.println("메시지 생성 실패: " + e.getMessage());
        }
    }

    static void validationTest(UserService userService) {
        System.out.println("\n--- [유효성 검사 테스트 시작] ---");
        try {
            userService.create("", "invalid@test.com");
        } catch (IllegalArgumentException e) {
            System.out.println("유효성 검사 성공 (예외 발생): " + e.getMessage());
        }
    }
}