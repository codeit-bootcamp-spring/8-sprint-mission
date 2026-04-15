package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "binary_contents")
public class BinaryContent extends BaseUpdatableEntity {

    @Column(
            name = "file_name",
            nullable = false
    )
    private String fileName;

    @Column(
            name = "size",
            nullable = false
    )
    private Long size;

    @Column(
            name = "content_type",
            nullable = false
    )
    private String contentType;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false
    )
    private BinaryContentStatus status;

    public BinaryContent(String fileName, Long size, String contentType, BinaryContentStatus status) {
        this.fileName = fileName;
        this.size = size;
        this.contentType = contentType;
        this.status = status;
    }

    // 상태를 변경하는 로직
    public void updateStatus(BinaryContentStatus status) {
        if (status != null) {
            this.status = status;
        }
    }
}
