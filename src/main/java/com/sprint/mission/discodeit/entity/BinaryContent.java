package com.sprint.mission.discodeit.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "binary_contents")
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BinaryContent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;
    
    @Column(name = "size", nullable = false)
    private Long fileSize;
    
    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;
    
    @Column(name = "bytes", nullable = false, columnDefinition = "BYTEA")
    private byte[] bytes; // PostgreSQL BYTEA 타입으로 저장

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public BinaryContent(String fileName, String contentType, Long fileSize, String base64Bytes) {
        this.id = UUID.randomUUID();
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        // Base64 문자열을 byte 배열로 변환
        if (base64Bytes != null && !base64Bytes.isEmpty()) {
            this.bytes = java.util.Base64.getDecoder().decode(base64Bytes);
        } else {
            this.bytes = new byte[0];
        }
        this.createdAt = Instant.now();
    }
}