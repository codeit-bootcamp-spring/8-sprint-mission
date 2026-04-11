package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.MessageApi;
import com.sprint.mission.discodeit.dto.data.MessageDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.MessageService;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/messages")
public class MessageController implements MessageApi {

		private final MessageService messageService;

		/**
		 * 메시지 생성 요청 처리 (첨부 파일 선택).
		 */
		@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
		public ResponseEntity<MessageDto> create(
				@RequestPart(value = "messageCreateRequest", required = false) @Valid MessageCreateRequest messageCreateRequest,
				@RequestPart(value = "message", required = false) @Valid MessageCreateRequest messageRequestAlias,
				@RequestPart(value = "request", required = false) @Valid MessageCreateRequest genericRequestAlias,
				@RequestParam(value = "content", required = false) String content,
				@RequestParam(value = "channelId", required = false) UUID channelId,
				@RequestParam(value = "channelID", required = false) UUID channelIdAlias1,
				@RequestParam(value = "channel_id", required = false) UUID channelIdAlias2,
				@RequestParam(value = "channel", required = false) UUID channelIdAlias3,
				@RequestParam(value = "authorId", required = false) UUID authorId,
				@RequestPart(value = "attachments", required = false) List<MultipartFile> attachments,
				@RequestPart(value = "files", required = false) List<MultipartFile> filesAlias,
				@RequestPart(value = "images", required = false) List<MultipartFile> imagesAlias,
				@AuthenticationPrincipal DiscodeitUserDetails userDetails
		) {
				MessageCreateRequest resolvedRequest = Optional.ofNullable(messageCreateRequest)
						.orElse(Optional.ofNullable(messageRequestAlias).orElse(genericRequestAlias));
				UUID resolvedChannelId = Optional.ofNullable(channelId)
						.orElse(Optional.ofNullable(channelIdAlias1)
								.orElse(Optional.ofNullable(channelIdAlias2).orElse(channelIdAlias3)));
				log.debug("메시지 생성 파라미터, channelId={}, channelID={}, channel_id={}, channel={}, resolvedChannelId={}",
						channelId, channelIdAlias1, channelIdAlias2, channelIdAlias3, resolvedChannelId);
				if (resolvedRequest == null && content != null && resolvedChannelId != null) {
						UUID resolvedAuthorId = authorId;
						if (resolvedAuthorId == null && userDetails != null) {
								resolvedAuthorId = userDetails.getUserDto().id();
						}
						resolvedRequest = new MessageCreateRequest(content, resolvedChannelId, resolvedAuthorId);
				}
				if (resolvedRequest != null && resolvedRequest.authorId() == null && userDetails != null) {
						resolvedRequest = new MessageCreateRequest(
								resolvedRequest.content(),
								resolvedRequest.channelId(),
								userDetails.getUserDto().id()
						);
				}
				if (resolvedRequest == null) {
						throw new IllegalArgumentException("messageCreateRequest is required");
				}
				List<MultipartFile> resolvedAttachments = Optional.ofNullable(attachments)
						.orElse(Optional.ofNullable(filesAlias).orElse(imagesAlias));
				List<BinaryContentCreateRequest> attachmentRequests = Optional.ofNullable(resolvedAttachments)
						.map(files -> files.stream()
								.map(file -> {
										try {
												return new BinaryContentCreateRequest(
														file.getOriginalFilename(),
														file.getContentType(),
														file.getBytes()
												);
										} catch (IOException e) {
												throw new RuntimeException(e);
										}
								})
								.toList())
						.orElse(new ArrayList<>());
				log.debug("메시지 생성 요청, channelId={}, authorId={}, attachments={}",
						resolvedRequest.channelId(), resolvedRequest.authorId(),
						attachmentRequests.size());
				MessageDto createdMessage = messageService.create(resolvedRequest, attachmentRequests);
				return ResponseEntity
						.status(HttpStatus.CREATED)
						.body(createdMessage);
		}

		/**
		 * 메시지 수정 요청 처리.
		 */
		@PatchMapping(path = "{messageId}")
		public ResponseEntity<MessageDto> update(@PathVariable("messageId") UUID messageId,
				@RequestBody @Valid MessageUpdateRequest request) {
				log.debug("메시지 수정 요청, messageId={}", messageId);
				MessageDto updatedMessage = messageService.update(messageId, request);
				return ResponseEntity
						.status(HttpStatus.OK)
						.body(updatedMessage);
		}

		/**
		 * 메시지 삭제 요청 처리.
		 */
		@DeleteMapping(path = "{messageId}")
		public ResponseEntity<Void> delete(@PathVariable("messageId") UUID messageId) {
				log.debug("메시지 삭제 요청, messageId={}", messageId);
				messageService.delete(messageId);
				return ResponseEntity
						.status(HttpStatus.NO_CONTENT)
						.build();
		}

		@GetMapping
		public ResponseEntity<PageResponse<MessageDto>> findAllByChannelId(
				@RequestParam(value = "channelId", required = false) UUID channelId,
				@RequestParam(value = "channelID", required = false) UUID channelIdAlias1,
				@RequestParam(value = "channel_id", required = false) UUID channelIdAlias2,
				@RequestParam(value = "channel", required = false) UUID channelIdAlias3,
				@RequestParam(value = "cursor", required = false) Instant cursor,
				@PageableDefault(
						size = 50,
						page = 0,
						sort = "createdAt",
						direction = Direction.DESC
				) Pageable pageable) {
				UUID resolvedChannelId = Optional.ofNullable(channelId)
						.orElse(Optional.ofNullable(channelIdAlias1)
								.orElse(Optional.ofNullable(channelIdAlias2).orElse(channelIdAlias3)));
				log.debug("메시지 조회 파라미터, channelId={}, channelID={}, channel_id={}, channel={}, resolvedChannelId={}, cursor={}",
						channelId, channelIdAlias1, channelIdAlias2, channelIdAlias3, resolvedChannelId, cursor);
				if (resolvedChannelId == null) {
						throw new IllegalArgumentException("channelId is required");
				}
				PageResponse<MessageDto> messages = messageService.findAllByChannelId(resolvedChannelId, cursor,
						pageable);
				return ResponseEntity.ok(messages);
		}
}
