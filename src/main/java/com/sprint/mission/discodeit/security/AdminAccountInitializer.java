package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserRole;
import com.sprint.mission.discodeit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminAccountInitializer implements CommandLineRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(String... args) throws Exception {

    // 어드민 계정이 없는 경우에만 초기화
    if (!userRepository.existsByUsername("admin")) {
      User admin = new User(
          "admin",
          "admin@admin.com",
          passwordEncoder.encode("admin1234!"),
          null,
          UserRole.ADMIN
      );
      userRepository.save(admin);
    }
  }
}
