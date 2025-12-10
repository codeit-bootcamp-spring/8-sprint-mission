package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.util.UUID;

public class Message implements Serializable {
    private UUID id;
    private UUID uid;
    private UUID cid;
    private static final long serialVersionUID = 1L;
    private Long createdAt;
    private Long modifiedAt;
    private String channelName;
    private String from;
    private String content;


    public Message() {
        id = UUID.randomUUID();
        createdAt = System.currentTimeMillis();
    }

    public Message(UUID uid, UUID cid, String channelName, String from, String content) {
        this();
        this.uid = uid;
        this.cid = cid;
        this.channelName = channelName;
        this.from = from;
        this.content = content;
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

    public UUID getCid() {
        return cid;
    }

    public void setCid(UUID cid) {
        this.cid = cid;
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

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFileName() {
        return id.toString().concat(".ser");
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", uid=" + uid +
                ", cid=" + cid +
                ", createdAt=" + createdAt +
                ", modifiedAt=" + modifiedAt +
                ", channelName='" + channelName + '\'' +
                ", from='" + from + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
