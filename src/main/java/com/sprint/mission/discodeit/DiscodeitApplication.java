package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.dto.*;
import com.sprint.mission.discodeit.service.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Collections;
import java.util.List;

@SpringBootApplication
public class DiscodeitApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiscodeitApplication.class, args);
    }

    @Bean
    public CommandLineRunner run(UserService userService,
                                 ChannelService channelService,
                                 MessageService messageService,
                                 AuthService authService) {
        return args -> {
            try {
                System.out.println("\n--- DISCORD CLONE APPLICATION START (High-Level) ---");

                // 1. 유저 생성
                UserCreateRequest aliceRequest = new UserCreateRequest(
                        "Alice_Basic",
                        "alice@basic.com",
                        "password123!",
                        null,
                        null,
                        null);
                UserResponse alice = userService.create(aliceRequest);
                System.out.println("SETUP User 생성 완료: " + alice.getName());

                // [핵심] 파일 쓰기 안정화를 위해 3초간 대기합니다.
                System.out.println("데이터 동기화 대기 중 (3초)...");
                Thread.sleep(3000);

                // 2. 로그인 테스트 전 이메일 로그 출력
                System.out.println("로그인 시도 이메일: " + alice.getEmail());
                LoginRequest loginRequest = new LoginRequest(alice.getEmail(), "password123!");
                UserResponse loggedInUser = authService.login(loginRequest);
                System.out.println("LOGIN 성공: " + loggedInUser.getName() + " (" + loggedInUser.getEmail() + ")");

                // 3. PUBLIC 채널 생성
                ChannelCreateRequest publicDto = new ChannelCreateRequest(
                        "Public Notice",
                        "모두를 위한 공지사항 채널",
                        alice.getId(),
                        null
                );
                ChannelResponse publicChannel = channelService.createPublic(publicDto);
                System.out.println("PUBLIC Channel 생성 완료: " + publicChannel.getName());

                Thread.sleep(200);

                // 4. 메시지 생성 (고도화된 DTO 활용)
                MessageCreateRequest messageRequest = new MessageCreateRequest(
                        alice.getId(),
                        publicChannel.getId(),
                        "인증 및 고도화 서비스 테스트 성공!",
                        Collections.emptyList()
                );
                var message = messageService.create(messageRequest);
                System.out.println("메시지 전송 성공: [" + message.getContent() + "]");

                System.out.println("\n--- 모든 고도화 기능 테스트 성공 ---");

            } catch (Exception e) {
                System.err.println("테스트 도중 오류 발생: " + e.getMessage());
                // 상세 에러 확인이 필요한 경우 아래 주석 해제
                // e.printStackTrace();
            }
        };
    }
}