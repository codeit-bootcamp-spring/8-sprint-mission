package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "binary_contents")
@Getter
@NoArgsConstructor
public class BinaryContent extends BaseEntity {

  @Column(name = "file_name", nullable = false)
  private String fileName;

  @Column(nullable = false)
  private Long size;

  @Column(name = "content_type", nullable = false, length = 100)
  private String contentType;

  @Column(nullable = false)
  private byte[] bytes;

  // 생성자
  public BinaryContent(String fileName, String contentType, Long size, String bytes) {
    this.fileName = fileName;
    this.contentType = contentType;
    this.size = size;
    this.bytes = bytes != null ? bytes.getBytes() : new byte[0];
  }

  // update 메소드
  public void update(String fileName, String contentType, Long size, String bytes) {
    this.fileName = fileName;
    this.contentType = contentType;
    this.size = size;
    this.bytes = bytes != null ? bytes.getBytes() : new byte[0];
  }

  public Long getFileSize() {
    return size;
  }
}
