package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext; // ✅ IoC 컨테이너 객체
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DiscodeitApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiscodeitApplication.class, args);
    }

    /**
     * [멘토 피드백 반영]
     * 파라미터 주입 방식 대신 ApplicationContext(IoC 컨테이너)를 사용하여 빈을 조회합니다.
     */
    @Bean
    public CommandLineRunner run(ApplicationContext context) { // context를 인자로 받음
        return args -> {
            //  IoC 컨테이너 객체로부터 직접 Bean을 조회하도록 수정
            UserService userService = context.getBean(UserService.class);
            UserStatusService userStatusService = context.getBean(UserStatusService.class);
            ChannelService channelService = context.getBean(ChannelService.class);
            MessageService messageService = context.getBean(MessageService.class);

            System.out.println("========================================");
            System.out.println("Discodeit Application Started Successfully!");
            System.out.println("IoC Container로부터 모든 서비스를 정상적으로 로드했습니다.");
            System.out.println("========================================");

            // 초기 더미 데이터 생성이나 테스트 로직이 필요하다면 여기서 수행합니다.
        };
    }
}