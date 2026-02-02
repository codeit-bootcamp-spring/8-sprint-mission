package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import java.io.InputStream;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

/*
    [필드 설명]
    • put           : UUID 키 정보 바탕으로 byte[] 데이터를 저장 -> UUID (BinaryContent의 id)
    • get           : 키 정보를 바탕으로 byte[] 데이터를 읽어 InputStream 타입으로 반환 -> UUID (BinaryContent의 id)
    • download      : HTTP API로 다운로드 기능을 제공, BinaryContentDto 정보를 바탕으로 파일을 다운로드할 수 있는 응답을 반환
 */
public interface BinaryContentStorage {

  UUID put(UUID id, byte[] bytes);

  InputStream get(UUID id);

  ResponseEntity<?> download(BinaryContentDto dto);

}
