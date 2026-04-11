package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.dto.data.MessageDto;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.multipart.MultipartFile;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;

@Tag(name = "Message", description = "Message API")
public interface MessageApi {

		@Operation(summary = "Message 생성")
		@ApiResponses(value = {
				@ApiResponse(
						responseCode = "201", description = "Message가 성공적으로 생성됨",
						content = @Content(schema = @Schema(implementation = MessageDto.class))
				),
				@ApiResponse(
						responseCode = "404", description = "Channel 또는 User를 찾을 수 없음",
						content = @Content(examples = @ExampleObject(value = "Channel | Author with id {channelId | authorId} not found"))
				),
		})
		ResponseEntity<MessageDto> create(
				@Parameter(
						description = "Message 생성 정보",
						content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
				) MessageCreateRequest messageCreateRequest,
				@Parameter(
						description = "Message 생성 정보(별칭: message)",
						content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
				) MessageCreateRequest message,
				@Parameter(
						description = "Message 생성 정보(별칭: request)",
						content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
				) MessageCreateRequest request,
				@Parameter(description = "Message 내용(폼 필드 fallback)") String content,
				@Parameter(description = "채널 ID(폼 필드 fallback)") UUID channelId,
				@Parameter(description = "채널 ID 별칭(channelID)") UUID channelID,
				@Parameter(description = "채널 ID 별칭(channel_id)") UUID channel_id,
				@Parameter(description = "채널 ID 별칭(channel)") UUID channel,
				@Parameter(description = "작성자 ID(폼 필드 fallback)") UUID authorId,
				@Parameter(
						description = "Message 첨부 파일들",
						content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
				) List<MultipartFile> attachments,
				@Parameter(description = "Message 첨부 파일들(별칭: files)") List<MultipartFile> files,
				@Parameter(description = "Message 첨부 파일들(별칭: images)") List<MultipartFile> images,
				@AuthenticationPrincipal DiscodeitUserDetails userDetails
		);

		@Operation(summary = "Message 내용 수정")
		@ApiResponses(value = {
				@ApiResponse(
						responseCode = "200", description = "Message가 성공적으로 수정됨",
						content = @Content(schema = @Schema(implementation = MessageDto.class))
				),
				@ApiResponse(
						responseCode = "404", description = "Message를 찾을 수 없음",
						content = @Content(examples = @ExampleObject(value = "Message with id {messageId} not found"))
				),
		})
		ResponseEntity<MessageDto> update(
				@Parameter(description = "수정할 Message ID") UUID messageId,
				@Parameter(description = "수정할 Message 내용") MessageUpdateRequest request
		);

		@Operation(summary = "Message 삭제")
		@ApiResponses(value = {
				@ApiResponse(
						responseCode = "204", description = "Message가 성공적으로 삭제됨"
				),
				@ApiResponse(
						responseCode = "404", description = "Message를 찾을 수 없음",
						content = @Content(examples = @ExampleObject(value = "Message with id {messageId} not found"))
				),
		})
		ResponseEntity<Void> delete(
				@Parameter(description = "삭제할 Message ID") UUID messageId
		);

		@Operation(summary = "Channel의 Message 목록 조회", description = "항상 PageResponse 형태로 반환")
		@ApiResponses(value = {
				@ApiResponse(
						responseCode = "200", description = "Message 목록 조회 성공",
						content = @Content(schema = @Schema(description = "cursor 없음: MessageDto 배열, cursor 있음: PageResponse"))
				)
		})
		ResponseEntity<com.sprint.mission.discodeit.dto.response.PageResponse<MessageDto>> findAllByChannelId(
				@Parameter(description = "조회할 Channel ID") UUID channelId,
				@Parameter(description = "조회할 Channel ID 별칭(channelID)") UUID channelID,
				@Parameter(description = "조회할 Channel ID 별칭(channel_id)") UUID channel_id,
				@Parameter(description = "조회할 Channel ID 별칭(channel)") UUID channel,
				@Parameter(description = "페이징 커서 정보") Instant cursor,
				@Parameter(description = "페이징 정보", example = "{\"size\": 50, \"sort\": \"createdAt,desc\"}") Pageable pageable
		);
} 