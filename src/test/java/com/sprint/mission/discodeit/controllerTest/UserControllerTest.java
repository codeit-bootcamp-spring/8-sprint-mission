package com.sprint.mission.discodeit.controllerTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.mission.discodeit.DTO.dto.UserDto;
import com.sprint.mission.discodeit.DTO.request.UserCreateRequest;
import com.sprint.mission.discodeit.DTO.request.UserUpdateRequest;
import com.sprint.mission.discodeit.exception.UserException.UserNotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;

public class UserControllerTest extends ControllerTestSupport {

  @Test
  @WithMockUser
  @DisplayName("Post /api/users - 사용자 생성 성공")
  void createUser_success() throws Exception {
    // Given - 요청 데이터와 서비스 응답 설정
    UserCreateRequest request = new UserCreateRequest("test", "test@codeit.com", "password1234");
    UserDto userDto = new UserDto(UUID.randomUUID(), "test", "test@codeit.com", null, false);

    when(userService.create(any(UserCreateRequest.class), any())).thenReturn(userDto);

    MockMultipartFile userCreateRequestJson = new MockMultipartFile(
        "userCreateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    // when & Then - POST 요청 처리 검증
    mockMvc.perform(multipart("/api/users")
            .file(userCreateRequestJson)
            .with(csrf()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(userDto.id().toString())) // UUID를 문자열로 변환하여 비교
        .andExpect(jsonPath("$.username").value(userDto.username()))
        .andExpect(jsonPath("$.email").value(userDto.email()));

    // 서비스 메서드 호출 검증
    verify(userService).create(argThat(req ->
            req.username().equals(request.username()) &&
                req.email().equals(request.email())
        ), eq(Optional.empty()) // profile은 보내지 않았으므로 Optional.empty()로 검증
    );
  }

  @Test
  @WithMockUser
  @DisplayName("POST /api/users - 사용자 생성 실패 (유효성 검사)")
  void createUser_fail_validation() throws Exception {
    // given
    UserCreateRequest request = new UserCreateRequest("test", "test@codeit.com", "123");

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
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.details.password").exists());
  }

  @Test
  @WithMockUser
  @DisplayName("PATCH /api/users/{userId} - 사용자 정보 수정 성공")
  void updateUser_success() throws Exception {
    //given
    UUID userId = UUID.randomUUID();
    UserUpdateRequest request = new UserUpdateRequest("updatedUser", "updated@example.com",
        "newPassword");
    UserDto userDto = new UserDto(userId, "updatedUser", "updated@example.com", null, true);

    when(userService.update(eq(userId), any(UserUpdateRequest.class), any())).thenReturn(userDto);

    MockMultipartFile requestJson = new MockMultipartFile(
        "userUpdateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    // when & then
    mockMvc.perform(multipart("/api/users/{userId}", userId)
            .file(requestJson)
            .with(req -> {
              req.setMethod("PATCH");
              return req;
            })
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("updatedUser"))
        .andExpect(jsonPath("$.email").value("updated@example.com"));

    verify(userService).update(eq(userId), any(UserUpdateRequest.class), eq(Optional.empty()));
  }

  @Test
  @WithMockUser
  @DisplayName("DELETE /api/users/{userId} - 사용자 삭제 성공")
  void deleteUser_success() throws Exception {
    // given
    UUID userId = UUID.randomUUID();

    // when & then
    mockMvc.perform(delete("/api/users/{userId}", userId)
            .with(csrf()))
        .andExpect(status().isNoContent());
    verify(userService).delete(eq(userId));
  }

  @Test
  @WithMockUser
  @DisplayName("DELETE /api/users/{userId} - 사용자 삭제 실패 (존재하지 않는 유저)")
  void deleteUser_fail_notFound() throws Exception {
    //given
    UUID userId = UUID.randomUUID();
    willThrow(new UserNotFoundException(userId))
        .given(userService).delete(eq(userId));

    // when & then
    mockMvc.perform(delete("/api/users/{userId}", userId)
            .with(csrf()))
        .andExpect(status().isNotFound());
    verify(userService).delete(eq(userId));
  }

}
