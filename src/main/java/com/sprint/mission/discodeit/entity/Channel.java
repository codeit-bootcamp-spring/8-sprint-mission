package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class Channel implements Serializable {
    private UUID id;
    private UUID uid;
    private static final long serialVersionUID = 1L;
    private Long createdAt;
    private Long modifiedAt;
    private String name;
    private String host;
    private int participant;
    private List<String> participants;

    public Channel() {
        id = UUID.randomUUID();
        createdAt = System.currentTimeMillis();
    }

    public Channel(UUID uid, String name, String host, int participant, List<String> participants) {
        this();
        this.uid = uid;
        this.name = name;
        this.host = host;
        this.participant = participant;
        this.participants = participants;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUid() {
        return uid;
    }

    public void setUid(UUID uid) {
        this.uid = uid;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Long modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getParticipant() {
        return participant;
    }

    public void setParticipant(int participant) {
        this.participant = participant;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public String getFileName() {
        return id.toString().concat(".ser");
    }

    @Override
    public String toString() {
        return "Channel{" +
                "id=" + id +
                ", uid=" + uid +
                ", createdAt=" + createdAt +
                ", modifiedAt=" + modifiedAt +
                ", name='" + name + '\'' +
                ", host='" + host + '\'' +
                ", participant=" + participant +
                ", participants=" + participants +
                '}';
    }
}
