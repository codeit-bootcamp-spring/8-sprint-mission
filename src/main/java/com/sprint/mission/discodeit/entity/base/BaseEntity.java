package com.sprint.mission.discodeit.entity.base;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 모든 엔티티의 최상위 추상 클래스 [ ] id (UUID) [ ] createdAt (Instant) 속성 정의
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class) // JPA Auditing 기능을 활성화하여 시간을 자동으로 기록합니다.
public abstract class BaseEntity {

  @Id
  @Column(columnDefinition = "UUID")
  private UUID id;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  // 수동으로 ID를 할당해야 하는 경우를 위한 생성자 또는 메서드
  protected void setId(UUID id) {
    this.id = id;
  }
}