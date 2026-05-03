package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.NoSuchElementException;
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

  private static final String[][] SEED_USERS = {
      {"buzz", "buzz@codeit.com"},
      {"jessie", "jessie@codeit.com"},
      {"rex", "rex@codeit.com"},
      {"woody", "woody@codeit.com"}
  };

  private final UserService userService;
  private final UserRepository userRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentStorage binaryContentStorage;
  private final ChannelRepository channelRepository;
  private final ReadStatusRepository readStatusRepository;
  private final Environment environment;
  private final SetupProperties setupProperties;

  @PostConstruct
  public void init() {
    if (environment.getProperty("discodeit.init.skip-data", Boolean.class, false)) {
      return;
    }
    createSeedUsers();
    createAdminIfNotExists();
    ensureSeedChannelManagers();
    ensureSeedUserProfiles();
    createDefaultChannelAndReadStatuses();
  }

  private void ensureSeedChannelManagers() {
    // To make manual testing smoother, seed accounts can create public channels.
    for (String[] u : SEED_USERS) {
      String username = u[0];
      var user = userRepository.findByUsername(username)
          .orElseThrow(() -> new IllegalStateException("Seed user not found: " + username));
      if (user.getRole() == Role.USER) {
        user.updateRole(Role.CHANNEL_MANAGER);
        userRepository.save(user);
        log.info("Promoted seed user to CHANNEL_MANAGER: {}", username);
      }
    }
  }

  private void createAdminIfNotExists() {
    if (userRepository.existsByRole(Role.ADMIN)) {
      return;
    }

    SetupProperties.Admin admin = setupProperties.getAdmin();

    try {
      if (!userRepository.existsByUsername(admin.getUsername())) {
        userService.create(
            new UserCreateRequest(admin.getUsername(), admin.getEmail(), admin.getPassword()),
            Optional.empty()
        );
      }
      var foundAdmin = userRepository.findByUsername(admin.getUsername())
          .orElseThrow(() -> new IllegalStateException("Admin user not found after creation: " + admin.getUsername()));
      foundAdmin.updateRole(Role.ADMIN);
      userRepository.save(foundAdmin);
      log.info("Initialized admin account: {}", admin.getUsername());
    } catch (Exception e) {
      log.warn("Could not initialize admin account: {}", e.getMessage());
    }
  }

  /** DB에는 프로필이 있으나 로컬 스토리지 파일이 없는 경우(비동기 저장 실패 등). */
  private boolean seedProfileFileMissingOnDisk(BinaryContent profile) {
    try (InputStream in = binaryContentStorage.get(profile.getId())) {
      return in.read() == -1;
    } catch (NoSuchElementException e) {
      return true;
    } catch (IOException e) {
      log.debug("시드 프로필 파일 확인 실패 id={}: {}", profile.getId(), e.getMessage());
      return true;
    }
  }

  private void ensureSeedUserProfiles() {
    for (String[] u : SEED_USERS) {
      String username = u[0];
      // Use eager-fetch query to avoid LazyInitializationException on profile
      var user = userRepository.findByUsernameWithProfile(username)
          .orElseThrow(() -> new IllegalStateException("Seed user not found: " + username));

      loadSeedProfileImage(username).ifPresent(profileRequest -> {
        boolean needsProfileFix = user.getProfile() == null
            || user.getProfile().getSize() == null
            || user.getProfile().getSize() <= 10_000
            || seedProfileFileMissingOnDisk(user.getProfile());
        if (needsProfileFix) {
          try {
            BinaryContent binaryContent = new BinaryContent(
                profileRequest.fileName(),
                (long) profileRequest.bytes().length,
                profileRequest.contentType());
            binaryContentRepository.save(binaryContent);
            binaryContentStorage.put(binaryContent.getId(), profileRequest.bytes());
            binaryContent.updateStatus(BinaryContentStatus.SUCCESS);
            binaryContentRepository.save(binaryContent);
            user.update(user.getUsername(), user.getEmail(), null, binaryContent);
            userRepository.save(user);
            log.info("Updated profile for seed user: {}", username);
          } catch (Exception e) {
            log.warn("Could not update profile for {}: {}", username, e.getMessage());
          }
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
            new UserCreateRequest(username, email, setupProperties.getDefaultPassword()),
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

  /**
   * 프로필 시드 이미지: {@code static/{username}.png} 우선, 없으면 {@code seed-profiles/} 동일 이름(png/svg).
   */
  private Optional<BinaryContentCreateRequest> loadSeedProfileImage(String username) {
    try {
      ClassPathResource staticPng = new ClassPathResource("static/" + username + ".png");
      ClassPathResource seedPng = new ClassPathResource("seed-profiles/" + username + ".png");
      ClassPathResource staticSvg = new ClassPathResource("static/" + username + ".svg");
      ClassPathResource seedSvg = new ClassPathResource("seed-profiles/" + username + ".svg");
      byte[] bytes;
      String contentType;
      String fileName;
      if (staticPng.exists()) {
        try (InputStream is = staticPng.getInputStream()) {
          bytes = is.readAllBytes();
        }
        if (bytes.length == 0) {
          bytes = MINIMAL_PNG;
        }
        contentType = "image/png";
        fileName = username + ".png";
      } else if (seedPng.exists()) {
        try (InputStream is = seedPng.getInputStream()) {
          bytes = is.readAllBytes();
        }
        if (bytes.length == 0) {
          bytes = MINIMAL_PNG;
        }
        contentType = "image/png";
        fileName = username + ".png";
      } else if (staticSvg.exists()) {
        try (InputStream is = staticSvg.getInputStream()) {
          bytes = is.readAllBytes();
        }
        if (bytes.length == 0) {
          bytes = MINIMAL_PNG;
          contentType = "image/png";
          fileName = username + ".png";
        } else {
          contentType = "image/svg+xml";
          fileName = username + ".svg";
        }
      } else if (seedSvg.exists()) {
        try (InputStream is = seedSvg.getInputStream()) {
          bytes = is.readAllBytes();
        }
        if (bytes.length == 0) {
          bytes = MINIMAL_PNG;
          contentType = "image/png";
          fileName = username + ".png";
        } else {
          contentType = "image/svg+xml";
          fileName = username + ".svg";
        }
      } else {
        bytes = MINIMAL_PNG;
        contentType = "image/png";
        fileName = username + ".png";
      }
      return Optional.of(new BinaryContentCreateRequest(
          fileName,
          contentType,
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
      var user = userRepository.findByUsername(u[0])
          .orElseThrow(() -> new IllegalStateException("Seed user not found: " + u[0]));

      if (readStatusRepository.findByUserIdAndChannelId(user.getId(), defaultChannel.getId()).isEmpty()) {
        readStatusRepository.save(new ReadStatus(user, defaultChannel, now));
        log.debug("Created ReadStatus for user {} in default channel", user.getUsername());
      }
    }
  }

}
