package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import com.sprint.mission.discodeit.entity.base.BinaryContentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "binary_contents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BinaryContent extends BaseUpdatableEntity {

  @NotBlank
  @Column(name = "file_name", nullable = false)
  private String fileName;

  @NotNull
  @PositiveOrZero
  @Column(name = "size", nullable = false)
  private Long size;

  @NotBlank
  @Column(name = "content_type", length = 100, nullable = false)
  private String contentType;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private BinaryContentStatus status;

  public BinaryContent(String fileName, Long size, String contentType) {
    this.fileName = fileName;
    this.size = size;
    this.contentType = contentType;
    this.status = BinaryContentStatus.PROCESSING;
  }

  public void updateStatus(BinaryContentStatus status) {
    this.status = status;
  }
}
