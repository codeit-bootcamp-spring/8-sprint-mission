package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import org.springframework.http.ResponseEntity;

import java.io.InputStream;
import java.util.UUID;

/**
 * 바이너리 데이터의 저장/로드를 담당하는 컴포넌트입니다.
 * 저장 매체의 확장성(로컬 저장소, 원격 저장소)을 고려하여 설계되었습니다.
 */
public interface BinaryContentStorage {

    /**
     * UUID 키 정보를 바탕으로 byte[] 데이터를 저장합니다.
     * UUID는 BinaryContent의 Id 입니다.
     *
     * @param id 바이너리 콘텐츠의 고유 식별자 (BinaryContent의 Id)
     * @param bytes 저장할 바이너리 데이터
     * @return 저장된 바이너리 콘텐츠의 고유 식별자
     */
    UUID put(UUID id, byte[] bytes);

    /**
     * 키 정보를 바탕으로 byte[] 데이터를 읽어 InputStream 타입으로 반환합니다.
     * UUID는 BinaryContent의 Id 입니다.
     *
     * @param id 바이너리 콘텐츠의 고유 식별자 (BinaryContent의 Id)
     * @return 바이너리 데이터를 읽을 수 있는 InputStream
     */
    InputStream get(UUID id);

    /**
     * HTTP API로 다운로드 기능을 제공합니다.
     * BinaryContentDto 정보를 바탕으로 파일을 다운로드할 수 있는 응답을 반환합니다.
     *
     * @param binaryContentDto 바이너리 콘텐츠의 메타 정보를 담은 DTO
     * @return HTTP 응답 엔티티 (파일 다운로드용)
     */
    ResponseEntity<?> download(BinaryContentDto binaryContentDto);
}
