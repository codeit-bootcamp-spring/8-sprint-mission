package com.sprint.mission.discodeit.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.exception.GlobalExceptionHandler;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(com.sprint.mission.discodeit.controller.UserController.class)
@DisplayName("UserController 슬라이스 테스트")
class UserControllerTest {

  @org.springframework.boot.SpringBootConfiguration
  @org.springframework.context.annotation.Import({
      com.sprint.mission.discodeit.controller.UserController.class,
      GlobalExceptionHandler.class
  })
  static class Config {}

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @MockBean
  private UserService userService;
  @MockBean
  private UserStatusService userStatusService;
  @MockBean
  private BinaryContentService binaryContentService;

  private static final UUID USER_ID = UUID.randomUUID();

  @Nested
  @DisplayName("GET /api/users")
  class FindAll {

    @Test
    @DisplayName("성공: 사용자 목록 조회 시 200과 JSON 배열 반환")
    void success() throws Exception {
      UserDto dto = new UserDto(USER_ID, "user1", "a@b.com", null, false);
      given(userService.findAll()).willReturn(List.of(dto));

      mockMvc.perform(get("/api/users"))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$").isArray())
          .andExpect(jsonPath("$[0].username").value("user1"))
          .andExpect(jsonPath("$[0].email").value("a@b.com"));
    }

    @Test
    @DisplayName("성공: 사용자 없을 때 빈 배열 JSON 반환")
    void success_empty() throws Exception {
      given(userService.findAll()).willReturn(List.of());

      mockMvc.perform(get("/api/users"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isArray())
          .andExpect(jsonPath("$").isEmpty());
    }
  }

  @Nested
  @DisplayName("POST /api/users (application/json)")
  class CreateJson {

    @Test
    @DisplayName("성공: 유효한 JSON으로 사용자 생성 시 201과 생성된 사용자 JSON 반환")
    void success() throws Exception {
      UserCreateRequest request = new UserCreateRequest("user1", "a@b.com", "password");
      UserDto dto = new UserDto(USER_ID, "user1", "a@b.com", null, false);
      given(userService.create(any(UserCreateRequest.class), eq(Optional.empty()))).willReturn(dto);

      mockMvc.perform(post("/api/users")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.username").value("user1"))
          .andExpect(jsonPath("$.email").value("a@b.com"));
    }

    @Test
    @DisplayName("실패: 이메일 형식 오류 시 400과 검증 오류 JSON 반환")
    void fail_validation() throws Exception {
      String body = "{\"username\":\"u\",\"email\":\"invalid-email\",\"password\":\"p\"}";

      mockMvc.perform(post("/api/users")
              .contentType(MediaType.APPLICATION_JSON)
              .content(body))
          .andExpect(status().isBadRequest())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.code").exists())
          .andExpect(jsonPath("$.message").exists());
    }
  }

  @Nested
  @DisplayName("PATCH /api/users/{userId} (application/json)")
  class UpdateJson {

    @Test
    @DisplayName("성공: 유효한 JSON으로 수정 시 200과 수정된 사용자 JSON 반환")
    void success() throws Exception {
      UserUpdateRequest request = new UserUpdateRequest("newuser", "new@a.com", null);
      UserDto dto = new UserDto(USER_ID, "newuser", "new@a.com", null, false);
      given(userService.update(eq(USER_ID), any(UserUpdateRequest.class), eq(Optional.empty()))).willReturn(dto);

      mockMvc.perform(patch("/api/users/{userId}", USER_ID)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.username").value("newuser"))
          .andExpect(jsonPath("$.email").value("new@a.com"));
    }
  }

  @Nested
  @DisplayName("DELETE /api/users/{userId}")
  class Delete {

    @Test
    @DisplayName("성공: 사용자 삭제 시 204 No Content")
    void success() throws Exception {
      mockMvc.perform(delete("/api/users/{userId}", USER_ID))
          .andExpect(status().isNoContent());
    }
  }
}
