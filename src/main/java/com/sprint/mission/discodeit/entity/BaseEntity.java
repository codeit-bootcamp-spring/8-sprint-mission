package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;


/*
    BaseEntity
   - 공통 엔티티 속성을 관리하는 추상적 기반 클래스.

    [필드 설명]
    • id(UUID)        : 각 엔티티의 고유 식별자. UUID.randomUUID()로 유니크한 값 생성.
    • createdAt(Long) : 객체 생성 시점의 유닉스 타임스탬프(초 단위).
    • updatedAt(Long) : 마지막 수정 시점의 유닉스 타임스탬프.

    [메서드]
    • updateCall()    : 수정 시 호출하여 updatedAt 값을 현재 시각으로 갱신.
 */

@Getter
public class BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final Long createdAt; // 유닉스 타임스탬프
    private Long updatedAt;

    public BaseEntity() {
        this.id = UUID.randomUUID();
        // 초 단위로 나타내기
        Long nowTime = System.currentTimeMillis() / 1000L;
        this.createdAt = nowTime;
        this.updatedAt = nowTime;
    }


    // 수정 시에 호출 한다.
    public void updateCall() {
        this.updatedAt = System.currentTimeMillis() / 1000L;
    }
}
