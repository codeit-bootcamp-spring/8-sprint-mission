package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final BinaryContentRepository binaryContentRepository;

    @PostConstruct
    public void init() {
        // 이미 사용자가 있으면 초기화하지 않음
        if (!userRepository.findAll().isEmpty()) {
            return;
        }

        // 초기 사용자 데이터 생성
        String[] usernames = {"jessie", "rex", "buzz", "woody"};
        String[] emails = {"jessie@codeit.com", "rex@codeit.com", "buzz@codeit.com", "woody@codeit.com"};

        for (int i = 0; i < usernames.length; i++) {
            // BinaryContent 생성
            BinaryContent profileContent = new BinaryContent();
            BinaryContent savedContent = binaryContentRepository.save(profileContent);

            // User 생성 및 저장
            User user = new User(
                    usernames[i],
                    emails[i],
                    "password", // 기본 비밀번호
                    savedContent.getId()
            );
            User savedUser = userRepository.save(user);

            // UserStatus 생성 및 저장 (온라인 상태로 설정)
            UserStatus status = new UserStatus(savedUser.getId());
            status.updateLastAccessAt(); // 온라인 상태로 만들기 위해 최근 접근 시간 갱신
            userStatusRepository.save(status);
        }
    }
}

