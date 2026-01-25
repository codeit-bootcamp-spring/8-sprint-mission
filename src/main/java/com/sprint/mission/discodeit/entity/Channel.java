package com.sprint.mission.discodeit.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "channels")
@Getter
@Setter
@NoArgsConstructor
public class Channel implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, columnDefinition = "channel_type")
    private ChannelType type;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    // ownerId와 memberIds는 DB 스키마에 없으므로 @Transient로 표시
    // 실제로는 ReadStatus를 통해 참여자 정보를 관리
    @Transient
    private UUID ownerId;
    
    @Transient
    private Set<UUID> memberIds;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (memberIds == null) {
            memberIds = new HashSet<>();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public Channel(String name, String description, ChannelType type, UUID ownerId) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.type = type;
        this.ownerId = ownerId;
        this.memberIds = new HashSet<>();
        this.createdAt = Instant.now();
    }

    // Service가 호출하는 상태 변경 메서드
    public void update(String newName, String newDescription) {
        if (newName != null) this.name = newName;
        if (newDescription != null) this.description = newDescription;
        this.updatedAt = Instant.now();
    }
}