package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.dto.channel.ChannelCreatePublicRequest;
import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class DiscodeitApplication {

    static UserDto setupUser(UserService userService) {
        // 프로필 이미지 X
        UserCreateRequest request = new UserCreateRequest("최준영1", "Junyoung1@gmail.com", "jun1234", null, null);
        return userService.create(request);
    }

    static ChannelDto setupChannel(ChannelService channelService) {
        ChannelCreatePublicRequest request = new ChannelCreatePublicRequest("새로 등록 된 채널", "새로 생성 된 채널입니다.");
        ChannelDto channel = channelService.createPublicChannel(request);
        return channel;
    }

    static void messageCreateTest(MessageService messageService, ChannelDto channel, UserDto author) {
        MessageCreateRequest request = new MessageCreateRequest(
                channel.id(),
                author.id(),
                "반가워유",
                null
        );
        MessageDto message = messageService.createMessage(request);
        System.out.println("메시지 생성: " + message.id());
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(DiscodeitApplication.class, args);

        UserService userService = context.getBean(UserService.class);
        ChannelService channelService = context.getBean(ChannelService.class);
        MessageService messageService = context.getBean(MessageService.class);

        UserDto user = setupUser(userService);
        ChannelDto channel = setupChannel(channelService);

        messageCreateTest(messageService, channel, user);


        System.out.println("=== JavaApplication 실행 완료 ===");

    }



}
