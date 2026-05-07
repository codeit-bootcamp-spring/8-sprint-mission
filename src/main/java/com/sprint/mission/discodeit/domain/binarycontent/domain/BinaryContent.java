package com.sprint.mission.discodeit.domain.binarycontent.domain;

import com.sprint.mission.discodeit.global.entity.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "binary_contents")
@Entity
public class BinaryContent extends BaseUpdatableEntity
{
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "size", nullable = false)
    private Long size;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BinaryContentStatus status;

    public BinaryContent(String fileName, Long size, String contentType, BinaryContentStatus status) {
        this.fileName = fileName;
        this.size = size;
        this.contentType = contentType;
        this.status = BinaryContentStatus.PROCESSING;
    }

    public void updateStatus(BinaryContentStatus newStatus) {
        this.status = newStatus;
    }
}
