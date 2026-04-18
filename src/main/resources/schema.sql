-- 1. 가장 하위 자식 테이블 (관계 매핑 테이블)
DROP TABLE IF EXISTS message_attachments;
DROP TABLE IF EXISTS persistent_logins CASCADE;

-- 2. 외래 키로 다른 테이블을 참조하고 있는 테이블들
DROP TABLE IF EXISTS messages;
DROP TABLE IF EXISTS read_statuses;

-- 3. 더 이상 자식이 없는 부모 테이블들
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS channels;
DROP TABLE IF EXISTS binary_contents;

CREATE TABLE channels
(
    id          uuid PRIMARY KEY,
    created_at  timestamp with time zone NOT NULL,
    updated_at  timestamp with time zone,
    name        varchar(100),
    description varchar(500),
    type        varchar(10)              NOT NULL CHECK (type IN ('PUBLIC', 'PRIVATE'))
);

CREATE TABLE binary_contents
(
    id           uuid PRIMARY KEY,
    created_at   timestamp with time zone NOT NULL,
    file_name    varchar(255)             NOT NULL,
    size         bigint                   NOT NULL,
    content_type varchar(100)             NOT NULL
);

CREATE TABLE users
(
    id         uuid PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone,
    username   varchar(50) UNIQUE       NOT NULL,
    email      varchar(100) UNIQUE      NOT NULL,
    password   varchar(60)              NOT NULL,
    profile_id uuid UNIQUE,
    role       varchar(20)              NOT NULL,

    CONSTRAINT fk_users_binary_contents
        FOREIGN KEY (profile_id)
            REFERENCES binary_contents (id)
            ON DELETE SET NULL
);

CREATE TABLE read_statuses
(
    id           uuid PRIMARY KEY,
    created_at   timestamp with time zone NOT NULL,
    updated_at   timestamp with time zone,
    user_id      uuid                     NOT NULL,
    channel_id   uuid                     NOT NULL,
    last_read_at timestamp with time zone NOT NULL,

    CONSTRAINT fk_read_statuses_users
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_read_statuses_channels
        FOREIGN KEY (channel_id)
            REFERENCES channels (id)
            ON DELETE CASCADE,

    CONSTRAINT uk_read_statuses_user_channel
        UNIQUE (user_id, channel_id)
);

CREATE TABLE messages
(
    id         uuid PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone,
    content    text,
    channel_id uuid                     NOT NULL,
    author_id  uuid,

    CONSTRAINT fk_messages_channels
        FOREIGN KEY (channel_id)
            REFERENCES channels (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_messages_users
        FOREIGN KEY (author_id)
            REFERENCES users (id)
            ON DELETE SET NULL
);

CREATE TABLE message_attachments
(
    message_id    uuid NOT NULL,
    attachment_id uuid NOT NULL,

    CONSTRAINT pk_message_attachments PRIMARY KEY (message_id, attachment_id),

    CONSTRAINT fk_message_attachments_messages
        FOREIGN KEY (message_id)
            REFERENCES messages (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_message_attachments_binary_contents
        FOREIGN KEY (attachment_id)
            REFERENCES binary_contents (id)
            ON DELETE CASCADE
);



