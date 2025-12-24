package com.sprint.mission.discodeit;
import com.sprint.mission.discodeit.DTO.request.*;
import com.sprint.mission.discodeit.DTO.response.ChannelResponse;
import com.sprint.mission.discodeit.DTO.response.MessageResponse;
import com.sprint.mission.discodeit.entity.ChannelStatus;
import com.sprint.mission.discodeit.service.*;
import com.sprint.mission.discodeit.DTO.response.UserResponse;
import com.sprint.mission.discodeit.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SpringBootApplication
public class DiscodeitApplication {
	static UserResponse setupUser(UserService userService) {
		UserCreateRequest createRequestDTO = new UserCreateRequest(
				"jaejoon0520",
				"이재준",
				"1234",
				"ljjn0520gmail.com",
				'남',
				3
		);

		byte[] bytes = new byte[] { (byte)0x89, 0x50, 0x4E, 0x47 };

		BinaryContentCreateRequest binaryContentCreateRequest = new BinaryContentCreateRequest(
				"아키",
				"image/png",
				bytes
		);

		return userService.create(createRequestDTO, Optional.of(binaryContentCreateRequest));
	}

	static UserResponse loginUser(AuthService authService) {
		LoginRequest reuest =  new LoginRequest("이재준", "1234");
		return authService.login(reuest);
	}

	static ChannelResponse setupPrivateChannel(ChannelService channelService, UserResponse author) {
		ChannelCreateRequest createRequestDTO = new ChannelCreateRequest(
				author.id(),
				ChannelStatus.PRIVATE,
				null,
				author.name(),
				null,
				2,
				List.of(author.name(), "한정우"),
				List.of()
		);
        return channelService.createPrivate(createRequestDTO);
    }

	static ChannelResponse setupPublicChannel(ChannelService channelService, UserResponse author) {
		ChannelCreateRequest createRequestDTO = new ChannelCreateRequest(
				author.id(),
				ChannelStatus.PUBLIC,
				"공지",
				author.name(),
				"공지 채널입니다.",
				1,
				List.of(author.name()),
				null
		);
		return channelService.createPublic(createRequestDTO);
	}

    static void messageCreateTest(MessageService messageService, ChannelResponse channel, UserResponse author) {
		MessageCreateRequest request = new MessageCreateRequest(author.id(), channel.id(), String.valueOf(channel.name()), author.name(), "안녕하세요?");
        MessageResponse message = messageService.create(request, new ArrayList<>());
		System.out.println(message);
		System.out.println("메시지 생성: " + message.id());
    }

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(DiscodeitApplication.class, args);

		//서비스 초기화
		UserService userService = context.getBean(UserService.class);
		ChannelService channelService = context.getBean(ChannelService.class);
		MessageService messageService = context.getBean(MessageService.class);
		AuthService authService = context.getBean(AuthService.class);

		// 셋업
		UserResponse user = setupUser(userService);
		System.out.println(user);
		UserResponse loginUser = loginUser(authService);
		System.out.println("로그인한 유저: " + loginUser);
		ChannelResponse publicChannel =  setupPublicChannel(channelService, user);
		ChannelResponse privateChannel = setupPrivateChannel(channelService, user);
		System.out.println(publicChannel);
		System.out.println(privateChannel);

		// 테스트
		messageCreateTest(messageService, publicChannel, user);
	}
}
