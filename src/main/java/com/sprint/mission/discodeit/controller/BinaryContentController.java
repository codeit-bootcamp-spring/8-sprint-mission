package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.BinaryContentApi;
import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import com.sprint.mission.discodeit.dto.data.BinaryContentWithBytesDto;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping({"/api/binaryContents", "/api/binary-contents"})
public class BinaryContentController implements BinaryContentApi {

		private final BinaryContentService binaryContentService;
		private final BinaryContentStorage binaryContentStorage;

		/**
		 * 첨부/프로필 조회 (바이트 포함, 미리보기용).
		 */
		@GetMapping(path = "{binaryContentId}")
		public ResponseEntity<BinaryContentWithBytesDto> find(
				@PathVariable("binaryContentId") UUID binaryContentId) {
				log.debug("바이너리 조회(바이트 포함) 요청, binaryContentId={}", binaryContentId);
				BinaryContentWithBytesDto withBytes = binaryContentService.findWithBytes(binaryContentId);
				return ResponseEntity
						.status(HttpStatus.OK)
						.body(withBytes);
		}

		@GetMapping
		public ResponseEntity<List<BinaryContentDto>> findAllByIdIn(
				@RequestParam("binaryContentIds") List<UUID> binaryContentIds) {
				List<BinaryContentDto> binaryContents = binaryContentService.findAllByIdIn(
						binaryContentIds);
				return ResponseEntity
						.status(HttpStatus.OK)
						.body(binaryContents);
		}

		/**
		 * 파일 다운로드 응답 반환.
		 */
		@GetMapping(path = "{binaryContentId}/download")
		public ResponseEntity<?> download(
				@PathVariable("binaryContentId") UUID binaryContentId) {
				log.debug("파일 다운로드 요청, binaryContentId={}", binaryContentId);
				BinaryContentDto binaryContentDto = binaryContentService.find(binaryContentId);
				ResponseEntity<?> response = binaryContentStorage.download(binaryContentDto);
				log.info("파일 다운로드 완료, binaryContentId={}, fileName={}", binaryContentId,
						binaryContentDto.fileName());
				return response;
		}
}
