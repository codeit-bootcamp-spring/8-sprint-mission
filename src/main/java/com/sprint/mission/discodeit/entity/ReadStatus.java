package com.sprint.mission.discodeit.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "read_statuses", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "channel_id"}))
@Getter
@Setter
@NoArgsConstructor
public class ReadStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
    
    @Column(name = "channel_id", nullable = false)
    private UUID channelId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", insertable = false, updatable = false)
    private Channel channel;
    
    @Column(name = "last_read_at", nullable = false)
    private Instant lastReadAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Transient
    private UUID lastReadMessageId; // DTO 변환용, DB에는 저장되지 않음

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (lastReadAt == null) {
            lastReadAt = Instant.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public ReadStatus(UUID userId, UUID channelId, UUID lastReadMessageId) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.channelId = channelId;
        this.lastReadMessageId = lastReadMessageId;
        this.lastReadAt = Instant.now();
        this.createdAt = Instant.now();
    }

    public ReadStatus(UUID userId, UUID channelId, Instant lastReadAt) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.channelId = channelId;
        this.lastReadAt = lastReadAt;
        this.createdAt = Instant.now();
    }

    public void updateLastReadMessage(UUID lastReadMessageId) {
        this.lastReadMessageId = lastReadMessageId;
        this.lastReadAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    public void updateLastReadAt(Instant lastReadAt) {
        this.lastReadAt = lastReadAt;
        this.updatedAt = Instant.now();
    }
}