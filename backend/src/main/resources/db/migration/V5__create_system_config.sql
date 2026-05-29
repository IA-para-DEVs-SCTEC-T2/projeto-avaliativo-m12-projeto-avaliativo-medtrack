CREATE TABLE system_config (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value VARCHAR(500),
    updated_at TIMESTAMP DEFAULT NOW(),
    updated_by UUID REFERENCES users(id)
);

-- Feature flags iniciais
INSERT INTO system_config (config_key, config_value) VALUES
    ('FDA_INTEGRATION_ENABLED', 'false'),
    ('NOTIFICATION_ENABLED', 'true');
