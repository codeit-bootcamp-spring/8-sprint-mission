package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.jcf.JCFChannelService;
import com.sprint.mission.discodeit.service.jcf.JCFMessageService;
import com.sprint.mission.discodeit.service.jcf.JCFUserService;

import java.util.List;

public class JavaApplication {

    public static void main(String[] args) {

        // 서비스 구현체 생성
        UserService userService = new JCFUserService();
        ChannelService channelService = new JCFChannelService();
        MessageService messageService = new JCFMessageService(userService, channelService);

        System.out.println("--------------- 등록 시작 ---------------");

        // 유저 등록
        User user = userService.create("최준영", "남", 30);
        userService.create("최준영10대", "남", 10);
        userService.create("최준영20대", "남", 20);
        userService.create("최준영30대", "남", 30);
        createOutput("user", user.getName());

        // 채널 등록
        Channel channel = channelService.createChannel("메인 채널", "처음 생성 된 해당 서비스의 메인 채널입니다.");
        createOutput("channel", channel.getName());

        // 메시지 등록
        Message message = messageService.createMessage(user.getId(), channel.getId(), "첫번째 메시지 입니다.");
        createOutput("message", message.getContents());

        System.out.println("--------------- 등록 종료 ---------------\n");

        System.out.println("--------------- 조회 시작 ---------------");

        User findUser = userService.findUser(user.getId());
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

        Channel findChannel = channelService.findChannel(channel.getId());
        System.out.printf("단건 조회로 조회 된 채널의 이름은 \"%s\", 채널 설명은 \"%s\" 입니다.\n", findChannel.getName(), findChannel.getDescription());
        System.out.println();

        List<Channel> allChannels = channelService.findAllChannels();
        System.out.println("다건 조회로 조회 된 채널의 정보입니다.");
        allChannels.forEach(System.out::println);
        System.out.println();

        Message findMessage = messageService.findMessage(message.getId());
        System.out.printf("단건 조회로 조회 된 메시지의 내용은 \"%s\" 입니다.\n", findMessage.getContents());
        System.out.println();

        List<Message> allMessages = messageService.findAllMessages();
        System.out.println("다건 조회로 조회 된 메시지의 정보입니다.");
        allMessages.forEach(System.out::println);
        System.out.println();

        System.out.println("--------------- 조회 종료 ---------------\n");

        System.out.println("--------------- 수정 시작 ---------------");

        User updateUser = userService.update(findUser.getId(), new User("김희영", "여", 40));
        System.out.printf("수정 된 유저의 이름은 \"%s\", 성별은 \"%s\", 나이는 \"%d\" 입니다.\n", updateUser.getName(), updateUser.getGender(), updateUser.getAge());

        Channel updateChannel = channelService.updateChannel(findChannel.getId(), "새로운 채널", "새로운 채널 이기에 많은 관심 부탁드립니다.");
        System.out.printf("수정 된 채널의 이름은 \"%s\", 설명은 \"%s\" 입니다.\n", updateChannel.getName(), updateChannel.getDescription());

        Message updateMessage = messageService.updateMessage(message.getId(), "수정 된 메시지");
        System.out.printf("수정 된 메시지는 \"%s\" 입니다.\n", updateMessage.getContents());
        System.out.println("--------------- 수정 종료 ---------------\n");


        System.out.println("--------------- 삭제 및 삭제 후 조회 시작 ---------------");

        System.out.println("- 유저 삭제 전 데이터 -");
        List<User> userList2 = userService.findAll();
        userList2.forEach(System.out::println);
        userService.delete(user.getId());
        System.out.println();

        System.out.println("- 유저 삭제 후 데이터 - ");
        List<User> userList3 = userService.findAll();
        userList3.forEach(System.out::println);
        System.out.println();

        System.out.println("- 채널 삭제 전 데이터 -");
        List<Channel> allChannels2 = channelService.findAllChannels();
        allChannels2.forEach(System.out::println);
        channelService.deleteChannel(channel.getId());
        System.out.println();

        System.out.println("- 채널 삭제 후 데이터 -");
        List<Channel> allChannels3 = channelService.findAllChannels();
        allChannels3.forEach(System.out::println);
        System.out.println();

        System.out.println("- 메시지 삭제 전 데이터 -");
        List<Message> allMessages2 = messageService.findAllMessages();
        allMessages2.forEach(System.out::println);
        messageService.deleteMessage(message.getId());
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
