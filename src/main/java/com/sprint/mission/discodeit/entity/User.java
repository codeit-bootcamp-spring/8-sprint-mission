package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private final UUID id;
    private final Instant createdAt;
    private Instant modifiedAt;
    private String userId;
    private String name;
    private String password;
    private String email;
    private char gender;
    private int grade;
    private UUID profileId;
    

    public User(String userId, String name, String password, String email, char gender, int grade, UUID profileId) {
        id = UUID.randomUUID();
        createdAt = Instant.now();
        modifiedAt = createdAt;

        this.userId = userId;
        this.name = name;
        this.password = password;
        this.email = email;
        this.gender = gender;
        this.grade = grade;
        this.profileId = profileId;
    }

    public void update(String userId, String name, String password,  String email, char gender, int grade, UUID profileId) {
        boolean anyValueUpdated = false;

        if(userId != null && !userId.equals(this.userId)) {
            this.userId = userId;
            anyValueUpdated = true;
        }
        if(name != null && !name.equals(this.name)) {
            this.name = name;
            anyValueUpdated = true;
        }
        if(password != null && !password.equals(this.password)) {
            this.password = password;
            anyValueUpdated = true;
        }
        if(email != null && !email.equals(this.email)) {
            this.email = email;
            anyValueUpdated = true;
        }
        if(gender != this.gender) {
            this.gender = gender;
            anyValueUpdated = true;
        }
        if(grade != this.grade) {
            this.grade = grade;
            anyValueUpdated = true;
        }
        if(profileId != null && !profileId.equals(this.profileId)) {
            this.profileId = profileId;
        }
        if(anyValueUpdated) {
            this.modifiedAt = Instant.now();
        }
    }


    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", createdAt=" + createdAt +
                ", modifiedAt=" + modifiedAt +
                ", userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", gender=" + gender +
                ", grade=" + grade +
                ", profileId=" + profileId +
                '}';
    }
}
