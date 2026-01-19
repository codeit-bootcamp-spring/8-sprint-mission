package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
    파일 하나를 표현하는 순수한 데이터 덩어리
    파일의 바이너리 데이터를 표현하는 도메인 모델. (프로필 이미지 등)
    불변 객체로 둔다. -> 보통 파일을 수정할 땐 기존 파일을 삭제하고,
    새로운 파일을 끼워넣는 식으로 동작시킨다.

    [필드 설명]
    • fileName         : 파일 이름
    • size             : 파일 사이즈
    • contentType      : 컨텐츠의 타입
    • bytes            : 파일 데이터를 byte 배열로 표현
 */
@Entity
@Table(name = "binary_contents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BinaryContent extends BaseEntity {

  @Column(name = "file_name", nullable = false)
  private String fileName;

  @Column(nullable = false)
  private Long size;

  @Column(name = "content_type", nullable = false, length = 100)
  private String contentType;

  public BinaryContent(String fileName, Long size, String contentType) {
    this.fileName = fileName;
    this.size = size;
    this.contentType = contentType;
  }
}
