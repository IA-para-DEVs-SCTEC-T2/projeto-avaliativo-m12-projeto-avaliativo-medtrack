CREATE TABLE user_medications (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    medication_id BIGINT NOT NULL REFERENCES medications(id),
    dosage VARCHAR(100),
    frequency_value INTEGER,
    frequency_unit VARCHAR(20),
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    reminder_time TIME,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_user_medications_user ON user_medications(user_id);
CREATE INDEX idx_user_medications_medication ON user_medications(medication_id);
