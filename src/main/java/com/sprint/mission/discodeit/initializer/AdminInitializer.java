package com.sprint.mission.discodeit.initializer;

import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(ApplicationArguments args) {

    // 관리자가 있거나 관리자 이메일이 이미 있는 경우 true
    boolean adminExists = userRepository.existsByRole(Role.ADMIN)
        || userRepository.existsByEmail("admin@codeit.com");

    if (!adminExists) {
      log.info("[Initializer] 어드민 계정이 존재하지 않아 초기화를 시작합니다.");

      User admin = new User(
          "관리자",
          "admin@codeit.com",
          passwordEncoder.encode("qwer1234"),
          null
      );

      admin.updateRole(Role.ADMIN);

      userRepository.save(admin);

      log.info("[Initializer] 어드민 계정 생성 완료: {}", admin.getUsername());
    } else {
      log.info("[Initializer] 어드민 계정이 존재하여 초기화를 건너뜁니다.");
    }
  }
}
