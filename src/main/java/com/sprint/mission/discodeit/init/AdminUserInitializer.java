package com.sprint.mission.discodeit.init;

import com.sprint.mission.discodeit.DTO.dto.UserDto;
import com.sprint.mission.discodeit.DTO.request.UserCreateRequest;
import com.sprint.mission.discodeit.DTO.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.exception.UserException;
import com.sprint.mission.discodeit.service.AuthService;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class AdminUserInitializer implements ApplicationRunner {

    private final UserService userService;
    private final AuthService authService;

    @Value("${discodeit.admin.username}")
    private String username;

    @Value("${discodeit.admin.password}")
    private String password;

    @Value("${discodeit.admin.email}")
    private String email;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        UserCreateRequest request = new UserCreateRequest(username, email, password);
        try {
            UserDto admin = userService.create(request, Optional.empty());
            authService.updateRoleInternal(new UserRoleUpdateRequest(admin.id(), Role.ADMIN));
            log.info("관리자 계정이 성공적으로 생성되었습니다.");
        } catch (UserException.DuplicateUsernameException e) {
            log.warn("관리자 계정이 이미 존재합니다.");
        } catch (Exception e) {
            log.error("관리자 계정 생성 중 오류가 발생했습니다: {}", e.getMessage(), e);
        }
    }
}
