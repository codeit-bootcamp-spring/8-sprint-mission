package com.sprint.mission.discodeit.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
public class Message implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "author_id")
    private UUID authorId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", insertable = false, updatable = false)
    private User author;
    
    @Column(name = "channel_id", nullable = false)
    private UUID channelId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", insertable = false, updatable = false)
    private Channel channel;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "message_attachments",
        joinColumns = @JoinColumn(name = "message_id"),
        inverseJoinColumns = @JoinColumn(name = "attachment_id")
    )
    private List<BinaryContent> attachments = new ArrayList<>();
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Transient
    private List<UUID> attachmentIds; // DTO 변환용

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (updatedAt == null) {
            updatedAt = Instant.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public Message(UUID authorId, UUID channelId, String content, List<UUID> attachmentIds) {
        this.id = UUID.randomUUID();
        this.authorId = authorId;
        this.channelId = channelId;
        this.content = content;
        this.attachmentIds = attachmentIds;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void update(String content, List<UUID> attachmentIds) {
        this.content = content;
        this.attachmentIds = attachmentIds;
        this.updatedAt = Instant.now();
    }
}