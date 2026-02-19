package com.sprint.mission.discodeit.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("User 통합 테스트")
class UserIntegrationTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private UserStatusRepository userStatusRepository;

  @Test
  @DisplayName("성공: 회원가입 시 응답이 생성되며 DB에 유저가 저장되어야 한다")
  void createUser_Success() throws Exception {
    UserCreateRequest request = new UserCreateRequest("tester", "test@test.com", "password123");

    MockMultipartFile requestPart = new MockMultipartFile(
        "userCreateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)
    );

    mockMvc.perform(multipart("/api/users")
            .file(requestPart))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.username").value("tester"));

    assertThat(userRepository.findAll()).anyMatch(u -> u.getEmail().equals("test@test.com"));
  }

  @Test
  @DisplayName("실패: 중복된 이메일로 가입 시 에러와 중복 에러 코드를 반환한다")
  void createUser_Fail_Duplicate() throws Exception {

    User user = userRepository.save(new User("existing", "test@test.com", "password123", null));
    userStatusRepository.save(new UserStatus(user, Instant.now()));

    UserCreateRequest request = new UserCreateRequest("newuser", "test@test.com", "password123");
    MockMultipartFile requestPart = new MockMultipartFile(
        "userCreateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)
    );

    mockMvc.perform(multipart("/api/users")
            .file(requestPart))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("DUPLICATE_USER"));
  }

  @Test
  @DisplayName("성공: 전체 사용자 목록을 조회한다")
  void findAllUsers_Success() throws Exception {
    //Given
    User u1 = userRepository.save(new User("user1", "user1@test.com", "pw1", null));
    userStatusRepository.save(new UserStatus(u1, Instant.now()));

    User u2 = userRepository.save(new User("user2", "user2@test.com", "pw2", null));
    userStatusRepository.save(new UserStatus(u2, Instant.now()));

    //When & Then
    mockMvc.perform(get("/api/users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));
  }

  @Test
  @DisplayName("성공: 사용자의 정보를 수정한다")
  void updateUser_Success() throws Exception {
    //Given
    User user = userRepository.save(new User("oldName", "old@test.com", "pw12345678", null));
    userStatusRepository.save(new UserStatus(user, Instant.now()));

    String updateJson = "{" +
        "\"newUsername\":\"newName\"," +
        "\"newEmail\":\"new@test.com\"," +
        "\"newPassword\":\"newPassword123\"" +
        "}";

    MockMultipartFile requestPart = new MockMultipartFile(
        "userUpdateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        updateJson.getBytes(StandardCharsets.UTF_8)
    );

    MockMultipartFile profilePart = new MockMultipartFile(
        "profile",
        "test.png",
        MediaType.IMAGE_PNG_VALUE,
        new byte[0]
    );

    //When & Then
    mockMvc.perform(multipart("/api/users/{userId}", user.getId())
            .file(requestPart)
            .file(profilePart)
            .with(request -> {
              request.setMethod("PATCH");
              return request;
            }))
        .andExpect(status().isOk());

    assertThat(userRepository.findById(user.getId()).get().getUsername()).isEqualTo("newName");
  }

  @Test
  @DisplayName("성공: 사용자를 삭제한다")
  void deleteUser_Success() throws Exception {
    //Given
    User user = userRepository.save(new User("deleteMe", "delete@test.com", "pw", null));
    userStatusRepository.save(new UserStatus(user, Instant.now()));

    //When & Then
    mockMvc.perform(delete("/api/users/{userId}", user.getId()))
        .andExpect(status().isNoContent()); // 님의 서버가 삭제 성공 시 204를 주는지 확인 필요

    assertThat(userRepository.findById(user.getId())).isEmpty();
  }
}