package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.config.DemoData;
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

@SpringBootApplication
public class DiscodeitApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(DiscodeitApplication.class, args);

        UserService userService = context.getBean(UserService.class);
        ChannelService channelService = context.getBean(ChannelService.class);
        MessageService messageService = context.getBean(MessageService.class);

        User user = setupUser(userService);
        Channel channel = setupChannel(channelService);
        Message message = messageCreateTest(messageService, channel, user);

        // 생성한 User/Channel/Message를 DemoData로 묶기
        DemoData demoData = new DemoData(user, channel, message);

        // 조회
        runReadDemo(userService, channelService, messageService, demoData);

        // 수정
        runUpdateDemo(userService, channelService, messageService, demoData);

        // 삭제
        runDeleteDemo(userService, channelService, messageService, demoData);

        System.out.println("=== JavaApplication 실행 완료 ===");

    }

    static User setupUser(UserService userService) {
        User user = userService.create("최준영", "남", 30);
        return user;
    }

    static Channel setupChannel(ChannelService channelService) {

        Channel channel = channelService.createChannel("새로 등록 된 채널", "새로 생성 된 채널입니다.");
        return channel;
    }

    static Message messageCreateTest(MessageService messageService, Channel channel, User author) {

        Message message = messageService.createMessage(author.getId(), channel.getId(), "추가 메시지 입니다.");
        System.out.println("메시지 생성: " + message.getId() + " " + message.getContents());

        return message;
    }


    // 조회 메서드
    private static void runReadDemo(UserService userService,
                                    ChannelService channelService,
                                    MessageService messageService,
                                    DemoData demoData) {

        printSectionTitle("조회 시작");

        User findUser = userService.findUser(demoData.user().getId());
        System.out.printf("단건 조회로 조회 된 유저의 이름은 \"%s\", 성별은 \"%s\", 나이는 \"%d\" 입니다.\n", findUser.getName(), findUser.getGender(), findUser.getAge());
        System.out.println();

        List<User> userList = userService.findAll();
        System.out.println("다건 조회로 조회 된 유저의 정보입니다.");
        userList.forEach(System.out::println);
        System.out.println();

        List<User> userAgeList = userService.findAllAge();
        System.out.println("다건 조회 (나이대)로 조회 된 유저의 정보입니다.");
        userAgeList.forEach(System.out::println);
        System.out.println();

        List<User> manList = userService.findAllGender("남");
        System.out.println("다건 조회 ( 성별(남) )로 조회 된 유저의 정보입니다.");
        manList.forEach(System.out::println);
        System.out.println();

        List<User> womanList = userService.findAllGender("여");
        System.out.println("다건 조회 ( 성별(여) )로 조회 된 유저의 정보입니다.");
        womanList.forEach(System.out::println);
        System.out.println();

        Channel findChannel = channelService.findChannel(demoData.channel().getId());
        System.out.printf("단건 조회로 조회 된 채널의 이름은 \"%s\", 채널 설명은 \"%s\" 입니다.\n", findChannel.getName(), findChannel.getDescription());
        System.out.println();

        List<Channel> allChannels = channelService.findAllChannels();
        System.out.println("다건 조회로 조회 된 채널의 정보입니다.");
        allChannels.forEach(System.out::println);
        System.out.println();

        Message findMessage = messageService.findMessage(demoData.message().getId());
        System.out.printf("단건 조회로 조회 된 메시지의 내용은 \"%s\" 입니다.\n", findMessage.getContents());
        System.out.println();

        List<Message> allMessages = messageService.findAllMessages();
        System.out.println("다건 조회로 조회 된 메시지의 정보입니다.");
        allMessages.forEach(System.out::println);
        System.out.println();

        printSectionTitle("조회 종료");
    }

    // 수정 메서드
    private static void runUpdateDemo(UserService userService,
                                      ChannelService channelService,
                                      MessageService messageService,
                                      DemoData demoData) {

        printSectionTitle("수정 시작");

        User updateUser = userService.update(demoData.user().getId(), new User("김희영", "여", 40));
        System.out.printf("수정 된 유저의 이름은 \"%s\", 성별은 \"%s\", 나이는 \"%d\" 입니다.\n", updateUser.getName(), updateUser.getGender(), updateUser.getAge());

        Channel updateChannel = channelService.updateChannel(demoData.channel().getId(), "수정 채널", "새롭게 수정된 채널 이기에 많은 관심 부탁드립니다.");
        System.out.printf("수정 된 채널의 이름은 \"%s\", 설명은 \"%s\" 입니다.\n", updateChannel.getName(), updateChannel.getDescription());

        Message updateMessage = messageService.updateMessage(demoData.message().getId(), "수정 된 메시지");
        System.out.printf("수정 된 메시지는 \"%s\" 입니다.\n", updateMessage.getContents());

        printSectionTitle("수정 종료");


    }

    // 삭제 메서드
    private static void runDeleteDemo(UserService userService,
                                      ChannelService channelService,
                                      MessageService messageService,
                                      DemoData demoData) {

        printSectionTitle("삭제 및 삭제 후 조회 시작");


        System.out.println("- 유저 삭제 전 데이터 -");
        List<User> userList2 = userService.findAll();
        userList2.forEach(System.out::println);
        userService.delete(demoData.user().getId());
        System.out.println();

        System.out.println("- 유저 삭제 후 데이터 - ");
        List<User> userList3 = userService.findAll();
        userList3.forEach(System.out::println);
        System.out.println();

        System.out.println("- 채널 삭제 전 데이터 -");
        List<Channel> allChannels2 = channelService.findAllChannels();
        allChannels2.forEach(System.out::println);
        channelService.deleteChannel(demoData.channel().getId());
        System.out.println();

        System.out.println("- 채널 삭제 후 데이터 -");
        List<Channel> allChannels3 = channelService.findAllChannels();
        allChannels3.forEach(System.out::println);
        System.out.println();

        System.out.println("- 메시지 삭제 전 데이터 -");
        List<Message> allMessages2 = messageService.findAllMessages();
        allMessages2.forEach(System.out::println);
        messageService.deleteMessage(demoData.message().getId());
        System.out.println();

        System.out.println("- 메시지 삭제 후 데이터 -");
        List<Message> allMessages3 = messageService.findAllMessages();
        allMessages3.forEach(System.out::println);
        System.out.println();

        printSectionTitle("삭제 및 삭제 후 조회 종료");

    }

    private static void printSectionTitle(String title) {
        System.out.println("\n" +
                "====================================================\n" +
                "  🔹 " + title + "\n" +
                "====================================================\n");
    }

}
