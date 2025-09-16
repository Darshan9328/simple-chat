-- Database schema for the Private Chat Application
-- This file contains the SQL commands to set up the required tables

-- Users table (already exists, but included for completeness)
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Updated Messages table for private messaging
CREATE TABLE IF NOT EXISTS messages (
    id BIGSERIAL PRIMARY KEY,
    content TEXT NOT NULL,
    sender_username VARCHAR(255) NOT NULL,
    recipient_username VARCHAR(255) NOT NULL,
    conversation_id VARCHAR(255) NOT NULL,
    message_status VARCHAR(20) DEFAULT 'SENT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_username) REFERENCES users(username),
    FOREIGN KEY (recipient_username) REFERENCES users(username)
);

-- New Conversations table
CREATE TABLE IF NOT EXISTS conversations (
    id VARCHAR(255) PRIMARY KEY,
    user1_username VARCHAR(255) NOT NULL,
    user2_username VARCHAR(255) NOT NULL,
    last_message TEXT,
    last_message_time TIMESTAMP,
    last_message_sender VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user1_username) REFERENCES users(username),
    FOREIGN KEY (user2_username) REFERENCES users(username)
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_messages_conversation_id ON messages(conversation_id);
CREATE INDEX IF NOT EXISTS idx_messages_sender ON messages(sender_username);
CREATE INDEX IF NOT EXISTS idx_messages_recipient ON messages(recipient_username);
CREATE INDEX IF NOT EXISTS idx_messages_created_at ON messages(created_at);
CREATE INDEX IF NOT EXISTS idx_conversations_user1 ON conversations(user1_username);
CREATE INDEX IF NOT EXISTS idx_conversations_user2 ON conversations(user2_username);
CREATE INDEX IF NOT EXISTS idx_conversations_updated_at ON conversations(updated_at);

-- Sample data (optional - remove in production)
-- Create some test users
INSERT INTO users (username, password) VALUES 
    ('darshan', '$2a$10$example_hashed_password_1'),
    ('haresh', '$2a$10$example_hashed_password_2'),
    ('naresh', '$2a$10$example_hashed_password_3')
ON CONFLICT (username) DO NOTHING;