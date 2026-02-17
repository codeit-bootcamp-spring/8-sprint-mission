package com.sprint.mission.discodeit.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.controller.UserController;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController 단위 테스트")
class UserControllerTest {

  private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Mock
  private UserService userService;
  @Mock
  private UserStatusService userStatusService;
  @Mock
  private BinaryContentService binaryContentService;
  @InjectMocks
  private UserController userController;

  private static final UUID USER_ID = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
  }

  @Nested
  @DisplayName("GET /api/users")
  class FindAll {

    @Test
    @DisplayName("성공: 사용자 목록 조회 시 200 반환")
    void success() throws Exception {
      given(userService.findAll()).willReturn(List.of());

      mockMvc.perform(get("/api/users"))
          .andExpect(status().isOk());
    }
  }

  @Nested
  @DisplayName("POST /api/users (JSON)")
  class CreateJson {

    @Test
    @DisplayName("성공: JSON으로 사용자 생성 시 201 반환")
    void success() throws Exception {
      UserCreateRequest request = new UserCreateRequest("user1", "a@b.com", "password");
      UserDto dto = new UserDto(USER_ID, "user1", "a@b.com", null, false);
      given(userService.create(any(UserCreateRequest.class), eq(Optional.empty()))).willReturn(dto);

      mockMvc.perform(post("/api/users")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated());
    }
  }

  @Nested
  @DisplayName("PATCH /api/users/{userId} (JSON)")
  class UpdateJson {

    @Test
    @DisplayName("성공: JSON으로 사용자 수정 시 200 반환")
    void success() throws Exception {
      UserUpdateRequest request = new UserUpdateRequest("newuser", "new@a.com", null);
      UserDto dto = new UserDto(USER_ID, "newuser", "new@a.com", null, false);
      given(userService.update(eq(USER_ID), any(UserUpdateRequest.class), eq(Optional.empty()))).willReturn(dto);

      mockMvc.perform(patch("/api/users/{userId}", USER_ID)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk());
    }
  }

  @Nested
  @DisplayName("DELETE /api/users/{userId}")
  class Delete {

    @Test
    @DisplayName("성공: 사용자 삭제 시 204 반환")
    void success() throws Exception {
      mockMvc.perform(delete("/api/users/{userId}", USER_ID))
          .andExpect(status().isNoContent());
    }
  }
}
