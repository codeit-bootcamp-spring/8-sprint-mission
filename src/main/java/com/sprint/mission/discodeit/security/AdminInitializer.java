package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(String... args) {
    if (userRepository.findByUsername("admin").isEmpty()) {

      User admin = new User(
          "admin",
          "admin@discodeit.com",
          passwordEncoder.encode("admin1234"),
          null,
          Role.ADMIN
      );
      userRepository.save(admin);
    }
  }
}
