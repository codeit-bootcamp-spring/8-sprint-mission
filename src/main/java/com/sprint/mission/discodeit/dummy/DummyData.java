package com.sprint.mission.discodeit.dummy;

import com.sprint.mission.discodeit.entity.Channel;
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


}