package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.data.ChannelDto;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.service.ChannelService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
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
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelController {

		private final ChannelService channelService;

		/**
		 * User가 참여 중인 Channel 목록 조회 GET /api/channels?userId=...
		 */
		@GetMapping
		public List<ChannelDto> findAllByUserId(@RequestParam UUID userId) {
				return channelService.findAllByUserId(userId);
		}

		/**
		 * Public Channel 생성 POST /api/channels/public
		 */
		@PostMapping(value = "/public", consumes = {"application/json", "multipart/form-data"})
		public ResponseEntity<ChannelDto> createPublic(
				@RequestPart(value = "channelCreateRequest", required = false) @Valid PublicChannelCreateRequest requestPart,
				@RequestBody(required = false) @Valid PublicChannelCreateRequest requestBody) {
				PublicChannelCreateRequest request = requestPart != null ? requestPart : requestBody;
				if (request == null) {
						throw new IllegalArgumentException("PublicChannelCreateRequest가 필요합니다.");
				}
				return ResponseEntity.status(HttpStatus.CREATED).body(channelService.create(request));
		}

		/**
		 * Private Channel 생성 POST /api/channels/private
		 */
		@PostMapping(value = "/private", consumes = {"application/json", "multipart/form-data"})
		public ResponseEntity<ChannelDto> createPrivate(
				@RequestPart(value = "channelCreateRequest", required = false) @Valid PrivateChannelCreateRequest requestPart,
				@RequestBody(required = false) @Valid PrivateChannelCreateRequest requestBody) {
				PrivateChannelCreateRequest request = requestPart != null ? requestPart : requestBody;
				if (request == null) {
						throw new IllegalArgumentException("PrivateChannelCreateRequest가 필요합니다.");
				}
				return ResponseEntity.status(HttpStatus.CREATED).body(channelService.create(request));
		}

		/**
		 * Channel 정보 수정 PATCH /api/channels/{channelId}
		 */
		@PatchMapping("/{channelId}")
		public ChannelDto update(
				@PathVariable UUID channelId,
				@RequestBody @Valid PublicChannelUpdateRequest request) {
				return channelService.update(channelId, request);
		}

		/**
		 * Channel 삭제 DELETE /api/channels/{channelId}
		 */
		@DeleteMapping("/{channelId}")
		public ResponseEntity<Void> delete(@PathVariable UUID channelId) {
				channelService.delete(channelId);
				return ResponseEntity.noContent().build();
		}
}
