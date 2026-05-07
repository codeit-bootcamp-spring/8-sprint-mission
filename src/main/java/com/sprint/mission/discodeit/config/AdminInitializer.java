package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.enums.Role;
import com.sprint.mission.discodeit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

  private static final String ADMIN_DEFAULT_PASSWORD = "${admin.default-password}";

  @Value(ADMIN_DEFAULT_PASSWORD)
  private String adminPassword;

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(ApplicationArguments args) throws Exception {

    // 어드민 계정이 없을 때 초기화
    String adminUsername = "admin";

    if (userRepository.findByUsername(adminUsername).isEmpty()) {
      log.info("ADMIN 계정이 존재하지 않습니다. 새로 생성합니다.");

      try {
        // ADMIN 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(adminPassword);

        // User 엔티티 생성
        User adminUser = new User(
            adminUsername,
            "admin@discodeit.com",
            encodedPassword
        );

        // updateRole 메서드로 관리자 권한 부여
        adminUser.updateRole(Role.ADMIN);

        // db 저장
        userRepository.save(adminUser);

        log.info("ADMIN 계정 생성 완료 : username={}", adminUsername);
      } catch (DataIntegrityViolationException e) {
        log.info("다른 서버 인스턴스에서 ADMIN 계정을 이미 생성했습니다. 생성을 건너뜁니다.");
      }
    } else {
      log.info("ADMIN 계정이 존재합니다.");
    }
  }
}
