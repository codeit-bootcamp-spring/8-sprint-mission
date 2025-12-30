package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserStatusRequest;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// TODO: 개발 단계에서 데이터 생성 결과를 확인하기 위한 임시 컴포넌트입니다.
// 개발 완료 후 삭제 가능합니다.
@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final UserService userService;
    private final UserStatusService userStatusService;

    @PostConstruct
    public void init() {
        // 이미 사용자가 있으면 초기화하지 않음
        if (!userService.findAll().isEmpty()) {
            return;
        }

        // 초기 사용자 데이터 생성
        String[] usernames = {"jessie", "rex", "buzz", "woody"};
        String[] emails = {"jessie@codeit.com", "rex@codeit.com", "buzz@codeit.com", "woody@codeit.com"};

        for (int i = 0; i < usernames.length; i++) {
            // UserService를 사용하여 사용자 생성 (UserStatus도 자동 생성됨)
            UserCreateRequest createRequest = new UserCreateRequest(
                    usernames[i],
                    emails[i],
                    "password", // 기본 비밀번호
                    null, // fileName
                    null, // fileType
                    null, // fileSize
                    null  // profileImage
            );
            var userResponse = userService.create(createRequest);

            // UserStatusService를 사용하여 온라인 상태로 설정
            UserStatusRequest statusRequest = new UserStatusRequest(userResponse.getId());
            userStatusService.update(statusRequest);
        }
    }
}

