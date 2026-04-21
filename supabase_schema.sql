-- ============================================================
-- ChatApp Database Schema for Supabase (PostgreSQL)
-- Run this in: Supabase Dashboard → SQL Editor → New Query
-- ============================================================

-- Enable UUID extension (usually already enabled on Supabase)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================
-- 1. USERS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username      VARCHAR(50)  NOT NULL,
    email         VARCHAR(100) NOT NULL,
    password      VARCHAR(255) NOT NULL,
    public_key    TEXT,
    created_at    TIMESTAMP(6) NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email    UNIQUE (email)
);

-- ============================================================
-- 2. MESSAGES TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS messages (
    id                 UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sender_id          UUID         NOT NULL,
    receiver_id        UUID,
    group_id           UUID,
    encrypted_payload  TEXT         NOT NULL,
    encrypted_key      TEXT,
    "timestamp"        TIMESTAMP(6) NOT NULL DEFAULT NOW(),
    status             VARCHAR(20)  NOT NULL DEFAULT 'SENT',

    CONSTRAINT chk_message_status CHECK (status IN ('SENT', 'DELIVERED', 'READ'))
);

CREATE INDEX IF NOT EXISTS idx_message_sender    ON messages (sender_id);
CREATE INDEX IF NOT EXISTS idx_message_receiver  ON messages (receiver_id);
CREATE INDEX IF NOT EXISTS idx_message_group     ON messages (group_id);
CREATE INDEX IF NOT EXISTS idx_message_timestamp ON messages ("timestamp");

-- ============================================================
-- 3. CHAT_GROUPS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS chat_groups (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(100) NOT NULL,
    created_by  UUID         NOT NULL,
    created_at  TIMESTAMP(6) NOT NULL DEFAULT NOW()
);

-- ============================================================
-- 4. GROUP_MEMBERS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS group_members (
    id        UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    group_id  UUID        NOT NULL,
    user_id   UUID        NOT NULL,
    role      VARCHAR(20) NOT NULL DEFAULT 'MEMBER',

    CONSTRAINT uk_group_member UNIQUE (group_id, user_id),
    CONSTRAINT chk_group_role  CHECK (role IN ('ADMIN', 'MEMBER'))
);

-- ============================================================
-- 5. GROUP_ENCRYPTED_KEYS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS group_encrypted_keys (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    group_id            UUID NOT NULL,
    user_id             UUID NOT NULL,
    encrypted_group_key TEXT NOT NULL,

    CONSTRAINT uk_group_encrypted_key UNIQUE (group_id, user_id)
);

-- ============================================================
-- OPTIONAL: Foreign Key Constraints
-- (Hibernate doesn't create these by default with ddl-auto=update,
--  but they're good practice for data integrity)
-- ============================================================

-- Messages → Users
ALTER TABLE messages
    ADD CONSTRAINT fk_message_sender
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE messages
    ADD CONSTRAINT fk_message_receiver
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE SET NULL;

-- Messages → Groups
ALTER TABLE messages
    ADD CONSTRAINT fk_message_group
    FOREIGN KEY (group_id) REFERENCES chat_groups(id) ON DELETE CASCADE;

-- Chat Groups → Users (creator)
ALTER TABLE chat_groups
    ADD CONSTRAINT fk_group_creator
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE;

-- Group Members → Groups & Users
ALTER TABLE group_members
    ADD CONSTRAINT fk_gm_group
    FOREIGN KEY (group_id) REFERENCES chat_groups(id) ON DELETE CASCADE;

ALTER TABLE group_members
    ADD CONSTRAINT fk_gm_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Group Encrypted Keys → Groups & Users
ALTER TABLE group_encrypted_keys
    ADD CONSTRAINT fk_gek_group
    FOREIGN KEY (group_id) REFERENCES chat_groups(id) ON DELETE CASCADE;

ALTER TABLE group_encrypted_keys
    ADD CONSTRAINT fk_gek_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- ============================================================
-- VERIFICATION: Check all tables were created
-- ============================================================
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_name IN ('users', 'messages', 'chat_groups', 'group_members', 'group_encrypted_keys')
ORDER BY table_name;
