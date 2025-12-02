package com.sprint.mission.discodeit.dummy;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.UUID;

public class DummyData {


    // 유저 더미데이터
    public static final List<User> DEFAULT_USERS =
            List.of(
                    new User("김태희", "여", 40),
                    new User("김태리", "여", 30),
                    new User("신예은", "여", 20),
                    new User("필릭스", "남", 20),
                    new User("박보검", "남", 30),
                    new User("원빈", "남", 40),
                    new User("이병헌", "남", 50)
            );

    // 채널 더미데이터
    public static final List<Channel> DEFAULT_CHANNELS =
            List.of(
                    new Channel("메인 채널", "모든 중요사항은 메인 채널에 올려주십시오."),
                    new Channel("보조 채널 1", "보조 채널 1에선 자유롭게 대화를 나누십시오."),
                    new Channel("보조 채널 2", "보조 채널 2에선 20대만 이용 가능합니다."),
                    new Channel("보조 채널 3", "보조 채널 3에선 30대만 이용 가능합니다."),
                    new Channel("보조 채널 4", "보조 채널 4에선 40대만 이용 가능합니다."),
                    new Channel("보조 채널 5", "보조 채널 5에선 50대만 이용 가능합니다.")
            );

    // 메시지 더미데이터
    public static final List<Message> DEFAULT_MESSAGES =
            List.of(
                    new Message(DEFAULT_USERS.get(0).getId(), DEFAULT_CHANNELS.get(0).getId(), "첫번째 메시지 입니다."),
                    new Message(DEFAULT_USERS.get(0).getId(), DEFAULT_CHANNELS.get(0).getId(), "두번째 메시지 입니다."),
                    new Message(DEFAULT_USERS.get(1).getId(), DEFAULT_CHANNELS.get(1).getId(), "세번째 메시지 입니다."),
                    new Message(DEFAULT_USERS.get(2).getId(), DEFAULT_CHANNELS.get(2).getId(), "네번째 메시지 입니다."),
                    new Message(DEFAULT_USERS.get(3).getId(), DEFAULT_CHANNELS.get(3).getId(), "다섯번째 메시지 입니다.")
            );

}