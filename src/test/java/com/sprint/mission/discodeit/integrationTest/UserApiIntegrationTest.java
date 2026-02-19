package com.sprint.mission.discodeit.integrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.DTO.request.UserCreateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import jakarta.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserApiIntegrationTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private UserService userService;

  @Test
  @WithMockUser
  @DisplayName("POST /api/users - 사용자 생성 통합 테스트 성공")
  void createUser_integration_success() throws Exception {
    // given
    UserCreateRequest request = new UserCreateRequest(
        "test",
        "test@codeit.com",
        "password123"
    );
    MockMultipartFile requestJson = new MockMultipartFile(
        "userCreateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    // when & then
    mockMvc.perform(multipart("/api/users")
            .file(requestJson)
            .with(csrf()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.username").value("test"));

    User foundUser = userRepository.findByUsername("test").orElseThrow();
    assertThat(foundUser.getEmail()).isEqualTo("test@codeit.com");
  }

  @Test
  @WithMockUser
  @DisplayName("POST /api/users - 사용자 생성 통합 테스트 실패 (중복된 이메일)")
  void createUser_integration_fail_duplicateEmail() throws Exception {
    // given
    // 미리 같은 이메일의 사용자를 저장
    userRepository.save(new User(
            "test",
            "test@codeit.com",
            "password123",
            null
        )
    );

    UserCreateRequest request = new UserCreateRequest(
        "test1",
        "test@codeit.com",
        "password123");
    MockMultipartFile requestJson = new MockMultipartFile(
        "userCreateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    // when & then
    mockMvc.perform(multipart("/api/users")
            .file(requestJson)
            .with(csrf()))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("U003"));
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/users - 사용자 목록 조회 통합 테스트")
  void findAllUsers_integration_success() throws Exception {
    // given
    UserCreateRequest request1 = new UserCreateRequest(
        "test1",
        "test1@codeit.com",
        "password123"
    );

    UserCreateRequest request2 = new UserCreateRequest(
        "test2",
        "test2@codeit.com",
        "password123"
    );

    userService.create(request1, Optional.empty());
    userService.create(request2, Optional.empty());

    // when & then
    mockMvc.perform(get("/api/users")
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].username").value("test1"))
        .andExpect(jsonPath("$[0].email").value("test1@codeit.com"))
        .andExpect(jsonPath("$[1].username").value("test2"))
        .andExpect(jsonPath("$[1].email").value("test2@codeit.com"));
  }

  @Test
  @WithMockUser
  @DisplayName("DELETE /api/users/{userId} - 사용자 삭제 통합 테스트")
  void deleteUser_integration_success() throws Exception {
    // given
    User savedUser = userRepository.save(new User(
        "test",
        "test@codeit.com",
        "password",
        null));

    // when & then
    mockMvc.perform(delete("/api/users/{userId}", savedUser.getId())
            .with(csrf()))
        .andExpect(status().isNoContent());

    assertThat(userRepository.findById(savedUser.getId())).isEmpty();
  }

  @Test
  @WithMockUser
  @DisplayName("DELETE /api/users/{userId} - 사용자 삭제 통합 테스트 실패 (존재하지 않는 사용자")
  void deleteUser_integration_fail_userNotFound() throws Exception {
    // given
    UUID userId = UUID.randomUUID();

    mockMvc.perform(delete("/api/users/{userId}", userId))
        .andExpect(status().isNotFound());
  }
}
