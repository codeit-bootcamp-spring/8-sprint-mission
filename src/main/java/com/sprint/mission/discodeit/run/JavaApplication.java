package com.sprint.mission.discodeit.run;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.file.FileChannelRepository;
import com.sprint.mission.discodeit.repository.file.FileMessageRepository;
import com.sprint.mission.discodeit.repository.file.FileUserRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFChannelRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFMessageRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFUserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.basic.BasicChannelService;
import com.sprint.mission.discodeit.service.basic.BasicMessageService;
import com.sprint.mission.discodeit.service.basic.BasicUserService;
import com.sprint.mission.discodeit.service.file.FileChannelService;
import com.sprint.mission.discodeit.service.file.FileMessageService;
import com.sprint.mission.discodeit.service.file.FileUserService;
import com.sprint.mission.discodeit.service.jcf.JCFChannelService;
import com.sprint.mission.discodeit.service.jcf.JCFMessageService;
import com.sprint.mission.discodeit.service.jcf.JCFUserService;

import java.io.*;
import java.util.*;

public class JavaApplication {
    static User setupUser(UserService userService) {
        return userService.create("woody", "woody1234", "woody1234@codeit.com", '여', 1);
    }

    static Channel setupChannel(ChannelService channelService, User author) {
        return channelService.create(author.getId(), "문명6", "이재준", 1, Arrays.asList("이재준"));
    }

    static void messageCreateTest(MessageService messageService, Channel channel, User author) {
        Message message = messageService.create(author.getId(), channel.getId(), channel.getName(), author.getName(), "안녕하세요?");
        message.setModifiedAt(message.getCreatedAt());
        System.out.println("메시지 생성: " + message);

        //데이터 정합성을 위해 지움
        messageService.deleteMessage(message.getId());
    }

    //미리 데이터 저장
    private static void init(UserService userService, ChannelService channelService, MessageService messageService) {
        //User
        User user1 = new User("phw", "박현우", "phw@gmail.com", '남', 3);
        User user2 = new User("hjw", "한정우", "hjw@naver.com", '남', 4);
        User user3 = new User("csy", "최수연", "csy@hanmail.com", '남', 2);
        User user4 = new User("kih", "김이현", "kih@gmail.com", '여', 2);
        User user5 = new User("ndm", "나두민", "ndm@gmail.com", '남', 1);
        User user6 = new User("ysc", "윤성철", "ysc@naver.com", '남', 1);
        User user7 = new User("kwh", "권지현", "kjh@gmail.com", '여', 2);
        User user8 = new User("psh", "박순호", "psh@gmail.com", '남', 4);
        User user9 = new User("kty", "김태양", "kty@gmail.com", '남', 3);
        User user10 = new User("yyj", "양유지", "yyj@gmail.com", '여', 3);
        User user11 = new User("lsj", "이소정", "lsj@gmail.com", '여', 2);
        User user12 = new User("jhg", "장형근", "jhg@naver.com", '남', 1);
        User user13 = new User("lsy", "이상은", "lsy@hanmail.net", '여', 1);

        userService.createUser(user1);
        userService.createUser(user2);
        userService.createUser(user3);
        userService.createUser(user4);
        userService.createUser(user5);
        userService.createUser(user6);
        userService.createUser(user7);
        userService.createUser(user8);
        userService.createUser(user9);
        userService.createUser(user10);
        userService.createUser(user11);
        userService.createUser(user12);
        userService.createUser(user13);

        //Channel
        Channel channel1 = new Channel(user9.getId(), "배틀그라운드", user9.getName(), 3, Arrays.asList("김태양", "한정우", "윤성철"));
        Channel channel2 = new Channel(user2.getId(), "서든어택", user2.getName(), 5, Arrays.asList("한정우", "나두민", "김이현"));
        Channel channel3 = new Channel(user3.getId(), "롤", user3.getName(), 2, Arrays.asList("최수연", "윤성철"));
        Channel channel4 = new Channel(user9.getId(), "패스오브액자일2", user9.getName(), 4, Arrays.asList("김태양", "한정우", "박순호", "윤성철"));
        Channel channel5 = new Channel(user9.getId(), "카트라이더", user9.getName(), 6, Arrays.asList("김태양", "최수연", "김이현", "권지현", "양유지", "이소정"));

        channelService.createChannel(channel1);
        channelService.createChannel(channel2);
        channelService.createChannel(channel3);
        channelService.createChannel(channel4);
        channelService.createChannel(channel5);

        //Message
        Message message1 = new Message(user9.getId(), channel1.getId(), channel1.getName(), user9.getName(), "8시에 하자");
        Message message2 = new Message(user8.getId(), channel1.getId(), channel1.getName(), user8.getName(), "콜!!");
        Message message3 = new Message(user2.getId(), channel2.getId(), channel1.getName(), user2.getName(), "fire in the hole");
        Message message4 = new Message(user5.getId(), channel2.getId(), channel2.getName(), user5.getName(), "taking fire");
        Message message5 = new Message(user9.getId(), channel4.getId(), channel4.getName(), user9.getName(), "균열 돕시다");
        Message message6 = new Message(user2.getId(), channel4.getId(), channel4.getName(), user2.getName(), "맵핑해야함");
        Message message7 = new Message(user7.getId(), channel5.getId(), channel5.getName(), user7.getName(), "아이템전");
        Message message8 = new Message(user10.getId(), channel5.getId(), channel5.getName(), user10.getName(), "스피드전");

        messageService.createMessage(message1);
        messageService.createMessage(message2);
        messageService.createMessage(message3);
        messageService.createMessage(message4);
        messageService.createMessage(message5);
        messageService.createMessage(message6);
        messageService.createMessage(message7);
        messageService.createMessage(message8);
    }

    private static void printList(List<?> list) {
        list.forEach(System.out::println);
    }

    private static <K, V> void printMap(Map<K, List<V>> map) {
        for(Map.Entry<K, List<V>> entry : map.entrySet()) {
            System.out.println("[" + entry.getKey() + "]");
            entry.getValue().forEach(System.out::println);
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("discodeit 프로그램을 실행합니다.");

        //File I/O 데이터 Init
//        UserRepository userRepository1 = new FileUserRepository();
//        UserService userService1 = new FileUserService(userRepository1);
//
//        ChannelRepository channelRepository1 = new FileChannelRepository();
//        ChannelService channelService1 = new FileChannelService(channelRepository1, userService1);
//
//        MessageRepository messageRepository1 = new FileMessageRepository();
//        MessageService messageService1 = new FileMessageService(messageRepository1, userService1,  channelService1);
//
//        init(userService1, channelService1, messageService1);
        //init end

        UserRepository userRepository = new JCFUserRepository();
        UserService userService = new JCFUserService(userRepository);

        ChannelRepository channelRepository = new JCFChannelRepository();
        ChannelService channelService = new JCFChannelService(channelRepository, userService);

        MessageRepository messageRepository = new JCFMessageRepository();
        MessageService messageService = new JCFMessageService(messageRepository, userService,  channelService);
        init(userService, channelService, messageService);

        User user = new User("jj", "이재준", "jj@naver.com", '남', 3);
        Channel channel = new Channel(user.getId(), "아이온2", "이재준", 2, Arrays.asList("이재준", "나두민"));
        Message message = new Message(user.getId(), channel.getId(),"아이온2", "이재준", "서버 오픈");

        //객체별 UUID(id) 변수
        UUID uid = user.getId();
        UUID cid = channel.getId();
        UUID mid = message.getId();

        while(true) {
            System.out.println("조회하고 싶은 객체를 선택하세요. (U)ser, (C)annel, (M)essage (B)asic (Q)uit");
            char selectObject = br.readLine().charAt(0);
            selectObject = Character.toUpperCase(selectObject);

            switch (selectObject) {
                case 'Q':
                    System.out.println("프로그램을 종료합니다.");
                    return;
                case 'U':
                    System.out.println("1. JCF, 2. File I/O, 3. File I/O + Repository");
                    int task_u = Integer.parseInt(br.readLine());

                    System.out.println("================ 생성 ==================");

                    switch(task_u) {
                        case 1:
                            userService.createUser(user);

                            System.out.println("<생성된 유저 데이터>");
                            System.out.println("아이디: " + user.getUserId() + ", 이름: " + user.getName() +
                                    ", 이메일: " + user.getEmail() + ", 성: " + user.getGender() + ", 학년: " + user.getGrade());

                            System.out.println("================ 조회 ==================");
                            System.out.println("[단건 조회 - JCFUserService - createUser 생성 후 조회]: ");
                            System.out.println(userService.findById(uid));

                            System.out.println("[다건 조회 - 전체 조회]: ");
                            printList(userService.findUsers());
                            System.out.println("[다건 조회 - 학년별 유저 조회]: ");
                            printMap(userService.findUserByGrade());

                            System.out.println("================ 수정 ==================");
                            userService.updateUser(user.getId(), "grade", 4);
                            System.out.println("수정된 유저 데이터 조회");
                            System.out.println(userService.findById(uid));

                            System.out.println("================ 삭제 ==================");
                            userService.deleteUser(uid);
                            if (userService.findById(uid) == null) System.out.println("존재하지 않는 유저입니다.");

                            break;
                        case 2:
                            userRepository = new FileUserRepository();
                            userService = new FileUserService(userRepository);

                            userService.createUser(user);

                            System.out.println("<생성된 유저 데이터>");
                            System.out.println("아이디: " + user.getUserId() + ", 이름: " + user.getName() +
                                    ", 이메일: " + user.getEmail() + ", 성: " + user.getGender() + ", 학년: " + user.getGrade());

                            System.out.println("================ 조회 ==================");

                            System.out.println("[단건 조회 - FileUserService - createUser 생성 후 조회]: ");
                            System.out.println(userService.findById(uid));

                            System.out.println("[다건 조회 - 전체 조회]: ");
                            printList(userService.findUsers());
                            System.out.println("[다건 조회 - 학년별 유저 조회]: ");
                            printMap(userService.findUserByGrade());

                            System.out.println("================ 수정 ==================");
                            userService.updateUser(user.getId(), "grade", 4);
                            System.out.println("수정된 유저 데이터 조회");
                            System.out.println(userService.findById(uid));

                            System.out.println("================ 삭제 ==================");
                            userService.deleteUser(uid);
                            if (userService.findById(uid) == null) System.out.println("존재하지 않는 유저입니다.");

                            break;
                        case 3:
                            userRepository = new FileUserRepository();
                            userService = new FileUserService(userRepository);

                            userService.saveUser(user);

                            System.out.println("<생성된 유저 데이터>");
                            System.out.println("아이디: " + user.getUserId() + ", 이름: " + user.getName() +
                                    ", 이메일: " + user.getEmail() + ", 성: " + user.getGender() + ", 학년: " + user.getGrade());

                            System.out.println("================ 조회 ==================");
                            System.out.println("[단건 조회 - JCFUserRepository - saveUser를 생성 후 조회]: ");
                            System.out.println(userService.findById(uid));

                            System.out.println("[다건 조회 - 전체 조회]: ");
                            printList(userService.findUsers());
                            System.out.println("[다건 조회 - 학년별 유저 조회]: ");
                            printMap(userService.findUserByGrade());

                            System.out.println("================ 수정 ==================");
                            //FileUserRepository - findByUId, saveUser 사용
                            userService.saveUser(user);
                            System.out.println("수정된 유저 데이터 조회");
                            //FileUserRepository - findByUId 사용, 반환값 Optional<User>
                            System.out.println(userService.findByUId(uid));

                            System.out.println("================ 삭제 ==================");
                            userService.deleteUser(uid);
                            //FileUserRepository - findByUId 사용
                            if (userService.findByUId(uid).isEmpty()) System.out.println("존재하지 않는 유저입니다.");

                            break;
                        default:
                            System.out.println("없는 명령어 입니다.");
                    }

                    break;
                case 'C':
                    System.out.println("1. JCF, 2. File I/O, 3. File I/O + Repository");
                    int task_c = Integer.parseInt(br.readLine());
                    System.out.println("================ 생성 ==================");

                    Channel c;
                    user = new User("jj", "이재준", "jj@naver.com", '남', 3);

                    switch(task_c) {
                        case 1:
                            channelService.createChannel(channel);

                            System.out.println("<생성된 채널 데이터>");
                            System.out.println("채널명: " + channel.getName() + ", 호스트: " + channel.getHost() + ", 참여인원 수: " + channel.getParticipant() +
                                    ", 참여인원: " + channel.getParticipants());

                            System.out.println("================ 조회 ==================");
                            System.out.println("[단건 조회 - JCFChannelService - createChannel 생성 후 데이터 조회]: ");
                            System.out.println(channelService.findById(cid));

                            System.out.println("[다건 조회 - 전체 조회]: ");
                            printList(channelService.findChannels());
                            System.out.println("[다건 조회 - 채널명별 조회]: ");
                            printMap(channelService.findChannelByName());
                            System.out.println("[다건 조회 - 인기있는 TOP-N 채널 조회]: ");
                            printList(channelService.findChannelByTopNParticipant(3));
                            System.out.println("[다건 조회 - 채널에 있는 유저 오름차순 조회]: ");
                            printList(channelService.findChannelByParticipantsASC(cid));

                            System.out.println("================ 수정 ==================");
                            System.out.println("수정된 채널 데이터 조회");
                            channelService.updateChannel(cid, "name", "클레르: 33원정대");
                            System.out.println(channelService.findById(cid));

                            System.out.println("================ 삭제 ==================");
                            channelService.deleteChannel(cid);
                            if (channelService.findById(cid) == null) System.out.println("존재하지 않는 채널입니다.");

                            break;
                        case 2:
                            userRepository = new FileUserRepository();
                            userService = new FileUserService(userRepository);

                            channelRepository = new FileChannelRepository();
                            channelService = new FileChannelService(channelRepository, userService);

                            userService.createUser(user);

                            c = channelService.create(user.getId(), channel.getName(), user.getName(), channel.getParticipant(), channel.getParticipants());
                            channelService.saveChannel(c);

                            System.out.println("<생성된 채널 데이터>");
                            System.out.println("채널명: " + channel.getName() + ", 호스트: " + channel.getHost() + ", 참여인원 수: " + channel.getParticipant() +
                                    ", 참여인원: " + channel.getParticipants());
                            System.out.println("================ 조회 ==================");
                            System.out.println("[단건 조회 - FileChannelService - createChannel 생성 후 조회]: ");
                            System.out.println(channelService.findById(c.getId()));

                            System.out.println("[다건 조회 - 전체 조회]: ");
                            printList(channelService.findChannels());
                            System.out.println("[다건 조회 - 채널명별 조회]: ");
                            printMap(channelService.findChannelByName());
                            System.out.println("[다건 조회 - 인기있는 TOP-N 채널 조회]: ");
                            printList(channelService.findChannelByTopNParticipant(3));
                            System.out.println("[다건 조회 - 채널에 있는 유저 오름차순 조회]: ");
                            printList(channelService.findChannelByParticipantsASC(c.getId()));

                            System.out.println("================ 수정 ==================");
                            System.out.println("수정된 채널 데이터 조회");
                            channelService.updateChannel(c.getId(), "name", "클레르: 33원정대");
                            System.out.println(channelService.findById(c.getId()));

                            System.out.println("================ 삭제 ==================");
                            channelService.deleteChannel(c.getId());
                            if (channelService.findById(c.getId()) == null) System.out.println("존재하지 않는 채널입니다.");

                            //데이터 정합성을 위해 제거
                            userService.deleteUser(c.getUid());

                            break;
                        case 3:
                            userRepository = new FileUserRepository();
                            userService = new FileUserService(userRepository);

                            channelRepository = new FileChannelRepository();
                            channelService = new FileChannelService(channelRepository, userService);

                            userService.createUser(user);

                            c = channelService.create(user.getId(), channel.getName(), user.getName(), channel.getParticipant(), channel.getParticipants());
                            channelService.saveChannel(c);

                            System.out.println("<생성된 채널 데이터>");
                            System.out.println("채널명: " + channel.getName() + ", 호스트: " + channel.getHost() + ", 참여인원 수: " + channel.getParticipant() +
                                    ", 참여인원: " + channel.getParticipants());
                            System.out.println("================ 조회 ==================");

                            System.out.println("[단건 조회 - JCFChannelRepository - saveChannel를 생성 후 조회]: ");
                            System.out.println(channelService.findById(c.getId()));

                            System.out.println("[다건 조회 - 전체 조회]: ");
                            printList(channelService.findChannels());
                            System.out.println("[다건 조회 - 채널명별 조회]: ");
                            printMap(channelService.findChannelByName());
                            System.out.println("[다건 조회 - 인기있는 TOP-N 채널 조회]: ");
                            printList(channelService.findChannelByTopNParticipant(3));
                            System.out.println("[다건 조회 - 채널에 있는 유저 오름차순 조회]: ");
                            printList(channelService.findChannelByParticipantsASC(cid));

                            System.out.println("================ 수정 ==================");
                            System.out.println("수정된 채널 데이터 조회");
                            //FileChannelRepository - findByCid, savaChannel 사용
                            channelService.saveChannel(c);
                            //FileChannelRepository - findByCId 사용, 반환값 Optional<Channel>
                            System.out.println(channelService.findByCId(c.getId()));

                            System.out.println("================ 삭제 ==================");
                            channelService.deleteChannel(c.getId());
                            if (channelService.findByCId(c.getId()).isEmpty()) System.out.println("존재하지 않는 채널입니다.");

                            //데이터 정합성을 위해 제거
                            userService.deleteUser(c.getUid());

                            break;
                        default:
                            System.out.println("없는 명령어 입니다.");
                    }
                    break;
                case 'M':
                    System.out.println("1. JCF, 2. File I/O, 3. File I/O + Repository");
                    int task_m = Integer.parseInt(br.readLine());
                    System.out.println("================ 생성 ==================");

                    Message m;
                    userService.createUser(user);
                    channelService.createChannel(channel);

                    user = new User("jj", "이재준", "jj@naver.com", '남', 3);
                    channel = new Channel(user.getId(), "아이온2", "이재준", 2, Arrays.asList("이재준", "나두민"));

                    switch (task_m) {
                        case 1:
                            m = messageService.create(uid, cid,  channel.getName(), user.getName(), message.getContent());

                            System.out.println("<생성된 메시지 데이터>");
                            System.out.println("채널명: " + message.getChannelName() + ", 발신자: " + message.getFrom() +
                                    ", 메시지 내용: " + message.getContent());
                            System.out.println("================ 조회 ==================");
                            System.out.println("[단건 조회 - JCFMessageService - createMessage 생성 후 데이터 조회]: ");
                            System.out.println(messageService.findById(m.getId()));

                            System.out.println("[전체 조회]: ");
                            printList(messageService.findMessages());
                            System.out.println("[다건 조회 - 발신자별 조회]: ");
                            printMap(messageService.findMessagesByFrom());

                            System.out.println("================ 수정 ==================");
                            messageService.updateMessage(m.getId(), "content", "재밌어요");
                            System.out.println("수정된 메시지 데이터 조회");
                            System.out.println(messageService.findById(m.getId()));
                            System.out.println("================ 삭제 ==================");
                            messageService.deleteMessage(mid);
                            if (messageService.findById(mid) == null) System.out.println("존재하지 않는 메시지입니다.");

                            break;
                        case 2:
                            userRepository = new FileUserRepository();
                            userService = new FileUserService(userRepository);

                            channelRepository = new FileChannelRepository();
                            channelService = new FileChannelService(channelRepository, userService);

                            messageRepository = new FileMessageRepository();
                            messageService = new FileMessageService(messageRepository, userService, channelService);

                            userService.createUser(user);
                            channelService.createChannel(channel);

                            m = messageService.create(user.getId(), channel.getId(), channel.getName(), user.getName(), message.getContent());
                            messageService.saveMessage(m);

                            System.out.println("<생성된 메시지 데이터>");
                            System.out.println("채널명: " + message.getChannelName() + ", 발신자: " + message.getFrom() +
                                    ", 메시지 내용: " + message.getContent());
                            System.out.println("================ 조회 ==================");
                            System.out.println("[단건 조회 - FileMessageService - createMessage 생성 후 조회]: ");
                            System.out.println(messageService.findById(m.getId()));

                            System.out.println("[전체 조회]: ");
                            printList(messageService.findMessages());
                            System.out.println("[다건 조회 - 발신자별 조회]: ");
                            printMap(messageService.findMessagesByFrom());

                            System.out.println("================ 수정 ==================");
                            messageService.updateMessage(m.getId(), "content", "재밌어요");
                            System.out.println("수정된 메시지 데이터 조회");
                            System.out.println(messageService.findById(m.getId()));
                            System.out.println("================ 삭제 ==================");
                            messageService.deleteMessage(m.getId());
                            if (messageService.findById(m.getId()) == null) System.out.println("존재하지 않는 메시지입니다.");

                            //정합성을 위해 데이터 제거
                            userService.deleteUser(m.getUid());
                            channelService.deleteChannel(m.getCid());

                            break;
                        case 3:
                            userRepository = new FileUserRepository();
                            userService = new FileUserService(userRepository);

                            channelRepository = new FileChannelRepository();
                            channelService = new FileChannelService(channelRepository, userService);

                            messageRepository = new FileMessageRepository();
                            messageService = new FileMessageService(messageRepository,  userService, channelService);

                            userService.createUser(user);
                            channelService.createChannel(channel);

                            m = messageService.create(user.getId(), channel.getId(), channel.getName(), user.getName(), message.getContent());
                            messageService.saveMessage(m);

                            System.out.println("<생성된 메시지 데이터>");
                            System.out.println("채널명: " + message.getChannelName() + ", 발신자: " + message.getFrom() +
                                    ", 메시지 내용: " + message.getContent());
                            System.out.println("================ 조회 ==================");
                            System.out.println("[단건 조회 - JCFMessageRepository - saveMessage 생성 후 조회]: ");
                            System.out.println(messageService.findById(m.getId()));

                            System.out.println("[전체 조회]: ");
                            printList(messageService.findMessages());
                            System.out.println("[다건 조회 - 발신자별 조회]: ");
                            printMap(messageService.findMessagesByFrom());

                            System.out.println("================ 수정 ==================");
                            //FileMessageRepository - findByMid, savaMessage 사용
                            messageService.saveMessage(m);
                            //FileMessageRepository - findByMId 사용, 반환값 Optional<message>
                            System.out.println("수정된 메시지 데이터 조회");
                            System.out.println(messageService.findByMId(m.getId()));

                            System.out.println("================ 삭제 ==================");
                            messageService.deleteMessage(m.getId());
                            if (messageService.findByMId(m.getId()).isEmpty()) System.out.println("존재하지 않는 메시지입니다.");

                            //정합성을 위해 데이터 제거
                            userService.deleteUser(m.getUid());
                            channelService.deleteChannel(m.getCid());

                            break;
                        default:
                            System.out.println("없는 명령어 입니다.");
                    }
                    break;
                case 'B':
                    System.out.println("(1)Basic (2)JCF (3)File");

                    int i = Integer.parseInt(br.readLine());
                    switch (i) {
                        case 1:
                            //서비스 초기화
                            //TODO BASIC*SERVICE 구현체를 초기화하세요.
                            // Basic + JCFRepository
                            userRepository = new JCFUserRepository();
                            channelRepository = new JCFChannelRepository();
                            messageRepository = new JCFMessageRepository();

                            userService = new BasicUserService(userRepository);
                            channelService = new BasicChannelService(channelRepository);
                            messageService = new BasicMessageService(messageRepository);

                            //셋업
                            user = setupUser(userService);
                            channel = setupChannel(channelService, user);
                            messageCreateTest(messageService, channel, user);

                            userRepository = new FileUserRepository();
                            channelRepository = new FileChannelRepository();
                            messageRepository = new FileMessageRepository();


                            // Basic + FileRepository
                            //셋업
                            user = setupUser(userService);
                            userService.createUser(user);
                            channel = setupChannel(channelService, user);
                            channelService.createChannel(channel);
                            messageCreateTest(messageService, channel, user);

                            //데이터 정합성을 위해 지움
                            userService.deleteUser(user.getId());
                            channelService.deleteChannel(channel.getId());

                            break;
                        case 2:
                            //서비스 초기화
                            userRepository = new JCFUserRepository();
                            channelRepository = new JCFChannelRepository();
                            messageRepository = new JCFMessageRepository();

                            userService = new JCFUserService(userRepository);
                            channelService = new JCFChannelService(channelRepository, userService);
                            messageService = new JCFMessageService(messageRepository, userService, channelService);

                            //셋업
                            user = setupUser(userService);
                            channel = setupChannel(channelService, user);
                            messageCreateTest(messageService, channel, user);

                            break;
                        case 3:
                            //서비스 초기화
                            userRepository = new FileUserRepository();
                            channelRepository = new FileChannelRepository();
                            messageRepository = new FileMessageRepository();

                            userService = new FileUserService(userRepository);
                            channelService = new FileChannelService(channelRepository, userService);
                            messageService = new FileMessageService(messageRepository, userService, channelService);

                            //셋업
                            user = setupUser(userService);
                            userService.createUser(user);

                            channel = setupChannel(channelService, user);
                            channelService.createChannel(channel);

                            messageCreateTest(messageService, channel, user);

                            //데이터 정합성을 위해 지움
                            userService.deleteUser(user.getId());
                            channelService.deleteChannel(channel.getId());

                            break;
                        default:
                            System.out.println("없는 명령어 입니다.");
                    }

                    break;
                default :
                    System.out.println("다시 입력해주세요.");
            }
        }
    }
}
