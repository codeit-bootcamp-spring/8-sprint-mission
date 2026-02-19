package com.sprint.mission.discodeit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.data.UserStatusDto;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // 최신 버전용 어노테이션
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@MockitoBean(types = JpaMetamodelMappingContext.class) // Audit 에러 방지
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private UserService userService;

  @MockitoBean
  private UserStatusService userStatusService;

  @Nested
  @DisplayName("유저 생성 테스트")
  class CreateUser {

    @Test
    @DisplayName("성공: 프로필 사진과 유효한 데이터를 전송하면 유저가 생성된다")
    void success() throws Exception {
      // given
      UserDto response = new UserDto(UUID.randomUUID(), "tester", "test@test.com", null, true);
      given(userService.create(any(), any())).willReturn(response);

      MockMultipartFile profile = new MockMultipartFile("profile", "img.png", "image/png",
          "content".getBytes());
      String requestJson = "{\"username\":\"tester\", \"email\":\"test@test.com\", \"password\":\"pw12345678\"}";
      MockMultipartFile userCreateRequest = new MockMultipartFile("userCreateRequest", "",
          "application/json", requestJson.getBytes());

      // when & then
      mockMvc.perform(multipart("/api/users")
              .file(profile)
              .file(userCreateRequest))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.username").value("tester"))
          .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    @DisplayName("실패: 이메일 형식이 올바르지 않으면 400 에러를 반환한다")
    void fail_invalidEmail() throws Exception {
      String requestJson = "{\"username\":\"tester\", \"email\":\"invalid-email\", \"password\":\"pw12345678\"}";
      MockMultipartFile userCreateRequest = new MockMultipartFile("userCreateRequest", "",
          "application/json", requestJson.getBytes());

      mockMvc.perform(multipart("/api/users")
              .file(userCreateRequest))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("유저 정보 수정 테스트")
  class UpdateUser {

    @Test
    @DisplayName("성공: 유효한 변경 데이터를 전송하면 200 OK를 반환한다")
    void success() throws Exception {
      UUID userId = UUID.randomUUID();
      UserDto response = new UserDto(userId, "newTitle", "new@test.com", null, true);
      given(userService.update(any(), any(), any())).willReturn(response);

      String updateJson = "{\"newUsername\":\"newTitle\", \"newEmail\":\"new@test.com\", \"newPassword\":\"newpw1234\"}";
      MockMultipartFile userUpdateRequest = new MockMultipartFile("userUpdateRequest", "",
          "application/json", updateJson.getBytes());

      mockMvc.perform(multipart(HttpMethod.PATCH, "/api/users/{userId}", userId)
              .file(userUpdateRequest))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.username").value("newTitle"));
    }

    @Test
    @DisplayName("실패: 수정 시 이메일 형식이 올바르지 않으면 400 에러를 반환한다")
    void fail_invalidUpdateEmail() throws Exception {
      String updateJson = "{\"newUsername\":\"newTitle\", \"newEmail\":\"not-an-email\", \"newPassword\":\"newpw1234\"}";
      MockMultipartFile userUpdateRequest = new MockMultipartFile("userUpdateRequest", "",
          "application/json", updateJson.getBytes());

      mockMvc.perform(multipart(HttpMethod.PATCH, "/api/users/{userId}", UUID.randomUUID())
              .file(userUpdateRequest))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("유저 상태 수정 테스트")
  class UpdateStatus {

    @Test
    @DisplayName("성공: 활동 시간을 업데이트한다")
    void success() throws Exception {
      UUID userId = UUID.randomUUID();
      UUID statusId = UUID.randomUUID();

      UserStatusDto statusDto = new UserStatusDto(statusId, userId, Instant.now());

      given(userStatusService.updateByUserId(any(), any())).willReturn(statusDto);

      String requestBody = "{\"newLastActiveAt\":\"2026-02-10T10:00:00Z\"}";

      mockMvc.perform(patch("/api/users/{userId}/userStatus", userId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.userId").value(userId.toString()));
    }

    @Test
    @DisplayName("실패: 필수 값인 활동 시간이 누락되면 400 에러를 반환한다")
    void fail_nullTime() throws Exception {
      String requestBody = "{}";

      mockMvc.perform(patch("/api/users/{userId}/userStatus", UUID.randomUUID())
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("유저 전체 조회 테스트")
  class FindAll {

    @Test
    @DisplayName("성공: 모든 유저 목록을 반환한다")
    void success() throws Exception {
      UserDto user1 = new UserDto(UUID.randomUUID(), "user1", "u1@test.com", null, true);
      UserDto user2 = new UserDto(UUID.randomUUID(), "user2", "u2@test.com", null, false);
      given(userService.findAll()).willReturn(List.of(user1, user2));

      mockMvc.perform(get("/api/users"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.length()").value(2))
          .andExpect(jsonPath("$[0].username").value("user1"));
    }
  }

  @Nested
  @DisplayName("유저 삭제 테스트")
  class Delete {

    @Test
    @DisplayName("성공: 유저를 삭제하면 204 No Content를 반환한다")
    void success() throws Exception {
      UUID userId = UUID.randomUUID();

      mockMvc.perform(delete("/api/users/{userId}", userId))
          .andExpect(status().isNoContent());
    }
  }
}