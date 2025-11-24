-- Migration V1001: Create message_session table
CREATE TABLE IF NOT EXISTS message_session (
    id BIGSERIAL PRIMARY KEY,
    evolution_session_id BIGINT NOT NULL,
    message_content TEXT NOT NULL,
    current_event VARCHAR(100) NOT NULL,
    message_timestamp BIGINT NOT NULL,
    remote_jid VARCHAR(255) NOT NULL,
    from_me BOOLEAN NOT NULL DEFAULT FALSE,
    message_type VARCHAR(50),
    push_name VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_message_session_evolution_session 
        FOREIGN KEY (evolution_session_id) 
        REFERENCES evolution_sessions(id) 
        ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX idx_message_session_evolution_session_id ON message_session(evolution_session_id);
CREATE INDEX idx_message_session_current_event ON message_session(current_event);
CREATE INDEX idx_message_session_remote_jid ON message_session(remote_jid);
CREATE INDEX idx_message_session_created_at ON message_session(created_at);
