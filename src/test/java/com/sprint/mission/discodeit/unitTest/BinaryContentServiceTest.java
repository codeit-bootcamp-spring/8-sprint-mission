package com.sprint.mission.discodeit.unitTest;

import com.sprint.mission.discodeit.DTO.dto.BinaryContentDto;
import com.sprint.mission.discodeit.DTO.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.exception.BinaryContentException.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.basic.BasicBinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class BinaryContentServiceTest {

    @InjectMocks
    private BasicBinaryContentService binaryContentService;

    @Mock
    private BinaryContentRepository binaryContentRepository;
    @Mock
    private BinaryContentStorage binaryContentStorage;
    @Mock
    private BinaryContentMapper binaryContentMapper;

    private BinaryContent binaryContent;

    @BeforeEach
    void setUp() {
        binaryContent = new BinaryContent("test.jpg", 1024L, "image/jpeg");
        ReflectionTestUtils.setField(binaryContent, "id", UUID.randomUUID());
    }

    @Test
    @DisplayName("파일 생성 성공")
    void createBinaryContent_success() {
        // given
        byte[] contentBytes = "test content".getBytes();
        BinaryContentCreateRequest request = new BinaryContentCreateRequest("test.jpg", "image/jpeg", contentBytes);
        BinaryContentDto dto = new BinaryContentDto(binaryContent.getId(), "test.jpg", 1024L, "image/jpeg");

        given(binaryContentRepository.save(any(BinaryContent.class))).willReturn(binaryContent);
        given(binaryContentMapper.toDto(any(BinaryContent.class))).willReturn(dto);

        // when
        BinaryContentDto result = binaryContentService.create(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.fileName()).isEqualTo("test.jpg");

        then(binaryContentRepository).should().save(any(BinaryContent.class));
        then(binaryContentStorage).should().put(binaryContent.getId(), contentBytes);
    }

    @Test
    @DisplayName("파일 삭제 성공")
    void deleteBinaryContent_success() {
        // given
        UUID contentId = binaryContent.getId();
        given(binaryContentRepository.existsById(contentId)).willReturn(true);

        // when
        binaryContentService.delete(contentId);

        // then
        then(binaryContentRepository).should().deleteById(contentId);
    }

    @Test
    @DisplayName("파일 삭제 실패 - 존재하지 않는 파일")
    void deleteBinaryContent_fail_notFound() {
        // given
        UUID nonExistentId = UUID.randomUUID();
        given(binaryContentRepository.existsById(nonExistentId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> binaryContentService.delete(nonExistentId))
                .isInstanceOf(BinaryContentNotFoundException.class);
    }
}
