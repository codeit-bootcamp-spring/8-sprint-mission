-- 기존 테이블 삭제 (순서 중요: FK 참조 관계 역순)
DROP TABLE IF EXISTS message_attachments;
DROP TABLE IF EXISTS messages;
DROP TABLE IF EXISTS read_statuses;
DROP TABLE IF EXISTS user_statuses;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS channels;
DROP TABLE IF EXISTS binary_contents;

-- 1. 독립적인 테이블(참조가 없는) 먼저 생성
CREATE TABLE IF NOT EXISTS binary_contents
(
    id           uuid PRIMARY KEY,
    created_at   timestamp with time zone NOT NULL,
    file_name    VARCHAR(255)             NOT NULL,
    size         BIGINT                   NOT NULL,
    content_type VARCHAR(100)             NOT NULL
);

CREATE TABLE IF NOT EXISTS channels
(
    id          uuid PRIMARY KEY,
    created_at  timestamp with time zone NOT NULL,
    updated_at  timestamp with time zone,
    name        VARCHAR(100),
    description VARCHAR(500),
    type        VARCHAR(10)              NOT NULL,
    CONSTRAINT check_channel_type CHECK (type IN ('PUBLIC', 'PRIVATE')) -- postgres에 enum이 없으므로 check 제약조건으로 추가
);

-- 2. 참조가 있는 테이블 생성
CREATE TABLE IF NOT EXISTS users
(
    id         uuid PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone,
    username   VARCHAR(50)              NOT NULL UNIQUE,
    email      VARCHAR(100)             NOT NULL UNIQUE,
    password   VARCHAR(60)              NOT NULL,
    profile_id uuid UNIQUE,
    CONSTRAINT fk_users_profile FOREIGN KEY (profile_id) REFERENCES binary_contents (id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS user_statuses
(
    id             uuid PRIMARY KEY,
    created_at     timestamp with time zone NOT NULL,
    updated_at     timestamp with time zone,
    user_id        uuid UNIQUE              NOT NULL,
    last_active_at timestamp with time zone NOT NULL,
    CONSTRAINT fk_user_statuses_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS read_statuses
(
    id           uuid PRIMARY KEY,
    created_at   timestamp with time zone NOT NULL,
    updated_at   timestamp with time zone,
    user_id      uuid                     NOT NULL,
    channel_id   uuid                     NOT NULL,
    last_read_at timestamp with time zone NOT NULL,
    CONSTRAINT unique_user_channel UNIQUE (user_id, channel_id),
    CONSTRAINT fk_read_statuses_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_read_statuses_channel_id FOREIGN KEY (channel_id) REFERENCES channels (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS messages
(
    id         uuid PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone,
    content    text,
    channel_id uuid                     NOT NULL,
    author_id  uuid                     NOT NULL,
    CONSTRAINT fk_messages_channel_id FOREIGN KEY (channel_id) REFERENCES channels (id) ON DELETE CASCADE,
    CONSTRAINT fk_messages_author_id FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS message_attachments
(
    message_id    uuid NOT NULL,
    attachment_id uuid NOT NULL,
    PRIMARY KEY (message_id, attachment_id), -- 복합키 설정 (중복 매핑 방지, 인덱싱+)
    CONSTRAINT fk_message_attachments_message_id FOREIGN KEY (message_id) REFERENCES messages (id) ON DELETE CASCADE,
    CONSTRAINT fk_message_attachments_attachment_id FOREIGN KEY (attachment_id) REFERENCES binary_contents (id) ON DELETE CASCADE
);

-- 인덱스 추가
CREATE INDEX idx_messages_channel_created_at ON messages (channel_id, created_at DESC);
CREATE INDEX idx_read_statuses_user_id ON read_statuses (user_id);
CREATE INDEX idx_read_statuses_channel_id ON read_statuses (channel_id);

/*
users(profile_id) 1(only) : 1 or 0 binary_contents(id)
users(id) 1(only) : 1 user_statuses(user_id)
users(id) 1(only) : 0 or N read_statuses(user_id)
users(id) 1(only) : 0 or N messages(author_id)
channels(id) 1(only) : 0 or N messages(channel_id)
channels(id) 1(only) : 0 or N read_statuses(channel_id)
messages(id) 1(only) : 0 or N messages_attachments(message_id)
binary_contents(id) 1(only) : 0 or 1 messages_attachments(attachment_id)
*/
