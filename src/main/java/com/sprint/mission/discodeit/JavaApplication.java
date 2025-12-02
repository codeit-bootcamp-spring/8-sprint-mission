package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.config.AppConfig;
import com.sprint.mission.discodeit.config.DemoData;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;

import java.util.List;

/*
    JavaApplication

    - AppConfig
      • 구현체 생성 및 의존성 주입을 담당하는 설정 클래스.
      • 구현체가 변경되더라도 AppConfig만 수정하면 되며,
        JavaApplication은 인터페이스(UserService, ChannelService, MessageService)에만 의존한다.
        (구현체에 의존하지 않는다.)

    - DemoData (record)
      • 등록 과정에서 생성된 User, Channel, Message를 한 번에 전달하기 위한 DTO.
      • 기존에는 지역 변수로만 사용되던 값을 record로 묶어 메서드 간에 안전하게 전달하도록 리팩토링했다.
 */

public class JavaApplication {


    public static void main(String[] args) {

        AppConfig config = new AppConfig();

        UserService userService = config.getUserService();
        ChannelService channelService = config.getChannelService();
        MessageService messageService = config.getMessageService();

        // 등록 데모 실행 -> 결과(User/Channel/Message)를 DemoData로 받기
        DemoData demoData = runCreateDemo(userService, channelService, messageService);

        // 조회
        runReadDemo(userService, channelService, messageService, demoData);

        // 수정
        runUpdateDemo(userService, channelService, messageService, demoData);

        // 삭제
        runDeleteDemo(userService, channelService, messageService, demoData);

    }



    // 등록 메서드
    private static DemoData runCreateDemo(UserService userService,
                                          ChannelService channelService,
                                          MessageService messageService) {

        System.out.println("--------------- 등록 시작 ---------------");

        // 유저 등록
        User user = userService.create("최준영", "남", 30);
        createOutput("user", user.getName());

        // 채널 등록
        Channel channel = channelService.createChannel("새로 등록 된 채널", "새로 생성 된 채널입니다.");
        createOutput("channel", channel.getName());

        // 메시지 등록
        Message message = messageService.createMessage(user.getId(), channel.getId(), "새로 추가 된 메시지 입니다.");
        createOutput("message", message.getContents());

        System.out.println("--------------- 등록 종료 ---------------\n");

        return new DemoData(user, channel, message);
    }

    // 조회 메서드
    private static void runReadDemo(UserService userService,
                                    ChannelService channelService,
                                    MessageService messageService,
                                    DemoData demoData) {

        System.out.println("--------------- 조회 시작 ---------------");

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

        System.out.println("--------------- 조회 종료 ---------------\n");
    }

    // 수정 메서드
    private static void runUpdateDemo(UserService userService,
                                      ChannelService channelService,
                                      MessageService messageService,
                                      DemoData demoData) {

        System.out.println("--------------- 수정 시작 ---------------");

        User updateUser = userService.update(demoData.user().getId(), new User("김희영", "여", 40));
        System.out.printf("수정 된 유저의 이름은 \"%s\", 성별은 \"%s\", 나이는 \"%d\" 입니다.\n", updateUser.getName(), updateUser.getGender(), updateUser.getAge());

        Channel updateChannel = channelService.updateChannel(demoData.channel().getId(), "새로운 채널", "새로운 채널 이기에 많은 관심 부탁드립니다.");
        System.out.printf("수정 된 채널의 이름은 \"%s\", 설명은 \"%s\" 입니다.\n", updateChannel.getName(), updateChannel.getDescription());

        Message updateMessage = messageService.updateMessage(demoData.message().getId(), "수정 된 메시지");
        System.out.printf("수정 된 메시지는 \"%s\" 입니다.\n", updateMessage.getContents());
        System.out.println("--------------- 수정 종료 ---------------\n");

    }

    // 삭제 메서드
    private static void runDeleteDemo(UserService userService,
                                      ChannelService channelService,
                                      MessageService messageService,
                                      DemoData demoData) {

        System.out.println("--------------- 삭제 및 삭제 후 조회 시작 ---------------");

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

        System.out.println("--------------- 삭제 및 삭제 후 조회 종료 ---------------\n");

    }

    // 등록 완료 출력 메서드
    public static void createOutput(String entityName, String name) {
        switch (entityName) {
            case "user":
                System.out.printf("- 유저 \"%s\"이 등록 되었습니다.\n", name);
                break;

            case "channel":
                System.out.printf("- 채널 \"%s\"이 등록 되었습니다.\n", name);
                break;

            case "message":
                System.out.printf("- 메시지 \"%s\" 등록 되었습니다.\n", name);
                break;

            default:
                System.out.println("해당 엔티티가 존재 하지 않습니다.");
                break;
        }
    }

}
