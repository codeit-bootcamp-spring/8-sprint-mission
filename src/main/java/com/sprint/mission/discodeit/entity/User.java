package com.sprint.mission.discodeit.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String name;
    
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(name = "password", nullable = false, length = 60)
    private String password;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Column(name = "profile_id", unique = true)
    private UUID profileId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", insertable = false, updatable = false)
    private BinaryContent profile;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public User(String name, String email, String password, UUID profileId) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.email = email;
        this.password = password;
        this.profileId = profileId;
        this.createdAt = Instant.now();
    }

    public void update(String name, String email, String password) {
        if (name != null) this.name = name;
        if (email != null) this.email = email;
        if (password != null) this.password = password;
        this.updatedAt = Instant.now();
    }

    public void updateProfileId(UUID profileId) {
        this.profileId = profileId;
        this.updatedAt = Instant.now();
    }
}