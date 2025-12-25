package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.dto.*;
import com.sprint.mission.discodeit.service.AuthService;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Collections;
import java.util.UUID;

@SpringBootApplication
public class DiscodeitApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiscodeitApplication.class, args);
    }

    @Bean
    public CommandLineRunner test(UserService userService,
                                  AuthService authService,
                                  ChannelService channelService,
                                  MessageService messageService) {
        return args -> {
            try {
                System.out.println("\n--- DISCORD CLONE APPLICATION START (High-Level) ---");

                // 1. 유저 생성 (인자 6개: 이름, 이메일, 비밀번호, 파일명, 파일타입, 파일크기)
                UserCreateRequest aliceRequest = new UserCreateRequest(
                        "Alice_Basic",
                        "alice@basic.com",
                        "password123!",
                        null, null, null
                );
                UserResponse alice = userService.create(aliceRequest);
                System.out.println("SETUP User 생성 완료: " + alice.getName());

                // 2. 로그인 테스트
                LoginRequest loginRequest = new LoginRequest(alice.getEmail(), "password123!");
                UserResponse loggedInUser = authService.login(loginRequest);
                System.out.println("LOGIN 성공: " + loggedInUser.getName() + " (" + loggedInUser.getEmail() + ")");

                // 3. PUBLIC 채널 생성 테스트
                // 생성자 요구 규격: (String name, String description, UUID ownerId, List<UUID> memberIds)
                ChannelCreateRequest publicRequest = new ChannelCreateRequest(
                        "Public Notice",
                        "General Announcements",
                        alice.getId(), // ownerId
                        Collections.emptyList() // memberIds (null 대신 빈 리스트)
                );
                ChannelResponse publicChannel = channelService.createPublic(publicRequest);

                if (publicChannel != null) {
                    System.out.println("PUBLIC Channel 생성 완료: " + publicChannel.getName());
                }

                // 4. 메시지 전송 테스트
                // 생성자 요구 규격: (UUID userId, UUID channelId, String content, List<UUID> mentionIds)
                //  주의: 에러 로그에 따라 UUID(사용자), UUID(채널), String(내용) 순서로 배치
                MessageCreateRequest messageRequest = new MessageCreateRequest(
                        alice.getId(),         // userId
                        publicChannel.getId(), // channelId
                        "인증 및 고도화 서비스 테스트 성공!", // content
                        Collections.emptyList() // mentionIds
                );

                messageService.create(messageRequest);
                System.out.println("메시지 전송 성공: [" + messageRequest.getContent() + "]");

                System.out.println("\n--- 모든 고도화 기능 테스트 성공 ---");

            } catch (Exception e) {
                System.err.println("테스트 도중 오류 발생: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }
}