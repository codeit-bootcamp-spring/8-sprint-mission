package com.sprint.mission.discodeit.entity.base;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter // 자식 엔티티들이 접근 가능
@MappedSuperclass // 자식 엔티티들에게 매핑 정보를 전달
@EntityListeners(AuditingEntityListener.class) //LastModifiedDate 자동 갱신
public abstract class BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id")
  private UUID id;

  @CreatedDate
  @Column(
      name = "created_at",
      updatable = false,
      nullable = false
  )
  private Instant createdAt;
}
