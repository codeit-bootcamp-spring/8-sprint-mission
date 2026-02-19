package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@Profile("!test")
public class DataInitializer {

  private static final String DEFAULT_PASSWORD = "password";
  private static final String[][] SEED_USERS = {
      {"buzz", "buzz@codeit.com"},
      {"jessie", "jessie@codeit.com"},
      {"rex", "rex@codeit.com"},
      {"woody", "woody@codeit.com"}
  };

  private final UserService userService;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;
  private final ReadStatusRepository readStatusRepository;
  private final Environment environment;

  @PostConstruct
  public void init() {
    if (environment.getProperty("discodeit.init.skip-data", Boolean.class, false)) {
      return;
    }
    createSeedUsers();
    ensureSeedUserProfiles();
    createDefaultChannelAndReadStatuses();
  }

  private void ensureSeedUserProfiles() {
    for (String[] u : SEED_USERS) {
      String username = u[0];
      userRepository.findByUsername(username).ifPresent(user -> {
        if (user.getProfile() == null) {
          loadSeedProfileImage(username).ifPresent(profileRequest -> {
            try {
              userService.update(user.getId(),
                  new UserUpdateRequest(user.getUsername(), user.getEmail(), null),
                  Optional.of(profileRequest));
              log.info("Set profile for existing seed user: {}", username);
            } catch (Exception e) {
              log.warn("Could not set profile for {}: {}", username, e.getMessage());
            }
          });
        }
      });
    }
  }

  private void createSeedUsers() {
    for (String[] u : SEED_USERS) {
      String username = u[0];
      String email = u[1];
      if (userRepository.existsByUsername(username)) {
        log.debug("Seed user already exists: {}", username);
        continue;
      }
      try {
        Optional<BinaryContentCreateRequest> profileImage = loadSeedProfileImage(username);
        userService.create(
            new UserCreateRequest(username, email, DEFAULT_PASSWORD),
            profileImage
        );
        log.info("Created seed user: {} (profile: {})", username, profileImage.isPresent() ? "yes" : "no");
      } catch (Exception e) {
        log.warn("Could not create seed user {}: {}", username, e.getMessage());
      }
    }
  }

  /** Minimal 1x1 transparent PNG (fallback when seed-profiles/{username}.png not found). */
  private static final byte[] MINIMAL_PNG = new byte[]{
      (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
      0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
      0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
      0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4, (byte) 0x89,
      0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41, 0x54,
      0x78, (byte) 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00, 0x05,
      0x00, 0x01, 0x0D, 0x0A, 0x2D, (byte) 0xB4, 0x00, 0x00,
      0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, (byte) 0xAE, 0x42, 0x60, (byte) 0x82
  };

  private Optional<BinaryContentCreateRequest> loadSeedProfileImage(String username) {
    String path = "seed-profiles/" + username + ".png";
    try {
      ClassPathResource resource = new ClassPathResource(path);
      byte[] bytes;
      if (resource.exists()) {
        try (InputStream is = resource.getInputStream()) {
          bytes = is.readAllBytes();
        }
        if (bytes.length == 0) {
          bytes = MINIMAL_PNG;
        }
      } else {
        bytes = MINIMAL_PNG;
      }
      return Optional.of(new BinaryContentCreateRequest(
          username + ".png",
          "image/png",
          bytes
      ));
    } catch (IOException e) {
      log.debug("Seed profile image for {}: {}", username, e.getMessage());
      return Optional.of(new BinaryContentCreateRequest(
          username + ".png",
          "image/png",
          MINIMAL_PNG
      ));
    }
  }

  private void createDefaultChannelAndReadStatuses() {
    if (channelRepository.count() > 0) {
      return;
    }
    Channel defaultChannel = new Channel(ChannelType.PUBLIC, "일반", "일반 채팅");
    channelRepository.save(defaultChannel);
    log.info("Created default channel: {}", defaultChannel.getName());

    Instant now = Instant.now();
    for (String[] u : SEED_USERS) {
      userRepository.findByUsername(u[0]).ifPresent(user -> {
        if (readStatusRepository.findByUserIdAndChannelId(user.getId(), defaultChannel.getId()).isEmpty()) {
          readStatusRepository.save(new ReadStatus(user, defaultChannel, now));
          log.debug("Created ReadStatus for user {} in default channel", user.getUsername());
        }
      });
    }
  }
}
