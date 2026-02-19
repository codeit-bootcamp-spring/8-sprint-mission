package com.sprint.mission.discodeit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.data.ChannelDto;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.service.ChannelService;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // Spring Boot 3.4+ 최신 임포트
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ChannelController.class)
@MockitoBean(types = JpaMetamodelMappingContext.class) // Jpa Audit 기능 모킹
class ChannelControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ChannelService channelService;

  @Nested
  @DisplayName("공개 채널 생성 테스트")
  class CreatePublic {

    @Test
    @DisplayName("성공: 유효한 데이터로 공개 채널을 생성한다")
    void success() throws Exception {
      // given
      ChannelDto response = new ChannelDto(UUID.randomUUID(), ChannelType.PUBLIC, "공개채널", "설명",
          Collections.emptyList(), null);
      given(channelService.create(any(PublicChannelCreateRequest.class))).willReturn(response);

      String requestBody = "{\"name\":\"공개채널\", \"description\":\"설명\"}";

      // when & then
      mockMvc.perform(post("/api/channels/public")
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.name").value("공개채널"))
          .andExpect(jsonPath("$.type").value("PUBLIC"));
    }

    @Test
    @DisplayName("실패: 채널 이름이 2자 미만이면 400 에러를 반환한다")
    void fail_tooShortName() throws Exception {
      String requestBody = "{\"name\":\"A\", \"description\":\"설명\"}";

      mockMvc.perform(post("/api/channels/public")
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("비공개 채널 생성 테스트")
  class CreatePrivate {

    @Test
    @DisplayName("성공: 참여자 목록을 포함하여 비공개 채널을 생성한다")
    void success() throws Exception {
      ChannelDto response = new ChannelDto(UUID.randomUUID(), ChannelType.PRIVATE, "비공개채널", null,
          Collections.emptyList(), null);
      given(channelService.create(any(PrivateChannelCreateRequest.class))).willReturn(response);

      String requestBody = "{\"participantIds\":[\"" + UUID.randomUUID() + "\"]}";

      mockMvc.perform(post("/api/channels/private")
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.type").value("PRIVATE"));
    }

    @Test
    @DisplayName("실패: 참여자 목록이 비어있으면 400 에러를 반환한다")
    void fail_emptyParticipants() throws Exception {
      String requestBody = "{\"participantIds\":[]}"; // @NotEmpty 검증

      mockMvc.perform(post("/api/channels/private")
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("채널 수정 테스트")
  class Update {

    @Test
    @DisplayName("성공: 채널 정보를 정상적으로 수정한다")
    void success() throws Exception {
      UUID channelId = UUID.randomUUID();
      ChannelDto response = new ChannelDto(channelId, ChannelType.PUBLIC, "수정된이름", "수정된설명",
          Collections.emptyList(), null);
      given(channelService.update(any(), any())).willReturn(response);

      String requestBody = "{\"newName\":\"수정된이름\", \"newDescription\":\"수정된설명\"}";

      mockMvc.perform(patch("/api/channels/{channelId}", channelId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.name").value("수정된이름"));
    }

    @Test
    @DisplayName("실패: 변경할 이름이 공백이면 400 에러를 반환한다")
    void fail_blankName() throws Exception {
      String requestBody = "{\"newName\":\" \", \"newDescription\":\"설명\"}";

      mockMvc.perform(patch("/api/channels/{channelId}", UUID.randomUUID())
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("채널 목록 조회 테스트")
  class FindAll {

    @Test
    @DisplayName("성공: 특정 유저가 참여한 채널 목록을 가져온다")
    void success() throws Exception {
      UUID userId = UUID.randomUUID();
      ChannelDto channel = new ChannelDto(UUID.randomUUID(), ChannelType.PUBLIC, "테스트채널", null,
          List.of(), null);
      given(channelService.findAllByUserId(userId)).willReturn(List.of(channel));

      mockMvc.perform(get("/api/channels")
              .param("userId", userId.toString()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[0].name").value("테스트채널"));
    }
  }

  @Nested
  @DisplayName("채널 삭제 테스트")
  class Delete {

    @Test
    @DisplayName("성공: 채널 삭제 시 204 No Content를 반환한다")
    void success() throws Exception {
      mockMvc.perform(delete("/api/channels/{channelId}", UUID.randomUUID()))
          .andExpect(status().isNoContent());
    }
  }
}