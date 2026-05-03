package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import com.sprint.mission.discodeit.dto.data.BinaryContentWithBytesDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.event.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.List;
import java.util.Base64;
import java.io.IOException;
import java.io.File;
import java.nio.file.Files;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicBinaryContentService implements BinaryContentService {

		private final BinaryContentRepository binaryContentRepository;
		private final BinaryContentStorage binaryContentStorage;
		private final ApplicationEventPublisher applicationEventPublisher;
		private final BinaryContentMapper binaryContentMapper;

		/**
		 * 파일 업로드 (프로필 사진, 첨부파일 등).
		 */
		@Transactional
		@Override
		public BinaryContentDto create(BinaryContentCreateRequest request) {
				String fileName = request.fileName();
				byte[] bytes = request.bytes();
				String contentType = request.contentType();
				log.debug("파일 업로드 요청 fileName={}, size={}, contentType={}", fileName, bytes.length,
						contentType);
				BinaryContent binaryContent = new BinaryContent(fileName, (long) bytes.length, contentType);
				binaryContentRepository.save(binaryContent);

				try {
						File tempFile = File.createTempFile("upload-", ".tmp");
						Files.write(tempFile.toPath(), bytes);
						applicationEventPublisher.publishEvent(new BinaryContentCreatedEvent(binaryContent.getId(), tempFile));
				} catch (IOException e) {
						throw new RuntimeException("Failed to create temporary file for upload", e);
				}

				log.info("파일 업로드 완료, binaryContentId={}, fileName={}, size={}",
						binaryContent.getId(), fileName, bytes.length);

				return new BinaryContentDto(
						binaryContent.getId(),
						binaryContent.getFileName(),
						binaryContent.getSize(),
						binaryContent.getContentType()
				);
		}

		@Override
		public BinaryContentDto find(UUID binaryContentId) {
				return binaryContentRepository.findById(binaryContentId)
						.map(binaryContentMapper::toDto)
						.orElseThrow(() -> {
								log.warn("바이너리 조회 실패: 없음, binaryContentId={}", binaryContentId);
								return new NoSuchElementException(
										"BinaryContent with id " + binaryContentId + " not found");
						});
		}

		@Override
		public BinaryContentWithBytesDto findWithBytes(UUID binaryContentId) {
				BinaryContentDto dto = find(binaryContentId);
				try {
						byte[] bytes = binaryContentStorage.get(binaryContentId).readAllBytes();
						String base64 = Base64.getEncoder().encodeToString(bytes);
						return new BinaryContentWithBytesDto(
								dto.id(),
								dto.fileName(),
								dto.size(),
								dto.contentType(),
								base64
						);
				} catch (IOException e) {
						log.error("바이너리 읽기 실패, binaryContentId={}", binaryContentId, e);
						throw new RuntimeException("Failed to read binary content: " + binaryContentId, e);
				}
		}

		@Override
		public List<BinaryContentDto> findAllByIdIn(List<UUID> binaryContentIds) {
				return binaryContentRepository.findAllById(binaryContentIds).stream()
						.map(binaryContentMapper::toDto)
						.toList();
		}

		@Transactional
		@Override
		public void delete(UUID binaryContentId) {
				log.debug("바이너리 삭제 시도, binaryContentId={}", binaryContentId);
				if (!binaryContentRepository.existsById(binaryContentId)) {
						log.warn("바이너리 삭제 실패: 없음, binaryContentId={}", binaryContentId);
						throw new NoSuchElementException(
								"BinaryContent with id " + binaryContentId + " not found");
				}
				binaryContentRepository.deleteById(binaryContentId);
				log.info("바이너리 삭제 완료, binaryContentId={}", binaryContentId);
		}

		@Transactional(propagation = Propagation.REQUIRES_NEW)
		@Override
		public BinaryContentDto updateStatus(UUID binaryContentId, BinaryContentStatus status) {
				BinaryContent binaryContent = binaryContentRepository.findById(binaryContentId)
						.orElseThrow(() -> new NoSuchElementException("BinaryContent with id " + binaryContentId + " not found"));
				binaryContent.updateStatus(status);
				return binaryContentMapper.toDto(binaryContent);
		}
}
