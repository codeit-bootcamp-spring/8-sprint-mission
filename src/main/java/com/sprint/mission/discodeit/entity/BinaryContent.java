package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/*
    파일 하나를 표현하는 순수한 데이터 덩어리
    파일의 바이너리 데이터를 표현하는 도메인 모델. (프로필 이미지 등)
    불변 객체로 둔다. -> 보통 파일을 수정할 땐 기존 파일을 삭제하고,
    새로운 파일을 끼워넣는 식으로 동작시킨다.

    수정이 되면 안되기에 BaseEntity를 상속 받지 않았다.

    [필드 설명]
    • id               : 유저 id
    • createdAt        : 바이너리 데이터 (파일, 이미지 등) 생성 시간
    • fileName         : 파일 이름
    • data             : 파일 데이터를 byte 배열로 표현
    • hostUserId       : 유저의 아이디   (도메인 연결)
    • hostMessageId    : 메시지의 아이디 (도메인 연결)
 */
@Getter
public class BinaryContent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private UUID id;
    private Instant createdAt;

    private String fileName;
    private byte[] data;

    private UUID hostUserId;
    private UUID hostMessageId;

    public BinaryContent(String fileName, byte[] data, UUID hostUserId, UUID hostMessageId) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.fileName = fileName;
        this.data = data != null ? data.clone() : null;
        this.hostUserId = hostUserId;
        this.hostMessageId = hostMessageId;
    }
}
