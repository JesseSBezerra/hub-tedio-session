-- Migration V1000: Create evolution_sessions table
CREATE TABLE IF NOT EXISTS evolution_sessions (
    id BIGSERIAL PRIMARY KEY,
    instance_id BIGINT NOT NULL,
    remote_jid VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_evolution_sessions_instance 
        FOREIGN KEY (instance_id) 
        REFERENCES evolution_instances(id) 
        ON DELETE CASCADE,
    
    CONSTRAINT uk_evolution_sessions_instance_jid 
        UNIQUE (instance_id, remote_jid)
);

-- Create index for better query performance
CREATE INDEX idx_evolution_sessions_instance_id ON evolution_sessions(instance_id);
CREATE INDEX idx_evolution_sessions_remote_jid ON evolution_sessions(remote_jid);
CREATE INDEX idx_evolution_sessions_status ON evolution_sessions(status);
