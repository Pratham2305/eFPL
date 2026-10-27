-- =====================================================================
-- V2: Chat Conversations and Messages
-- =====================================================================

CREATE TABLE chat_conversations (
    id                      UUID PRIMARY KEY,
    title                   VARCHAR(255),
    created_at              TIMESTAMP DEFAULT NOW(),
    updated_at              TIMESTAMP DEFAULT NOW()
);

CREATE TABLE chat_messages (
    id                      UUID PRIMARY KEY,
    conversation_id         UUID NOT NULL REFERENCES chat_conversations(id) ON DELETE CASCADE,
    role                    VARCHAR(20) NOT NULL,
    content                 TEXT NOT NULL,
    intent                  VARCHAR(50),
    source                  VARCHAR(50) NOT NULL,
    created_at              TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_chat_messages_conv_id ON chat_messages(conversation_id);
