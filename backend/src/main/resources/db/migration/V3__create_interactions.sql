CREATE TABLE interactions (
    id BIGSERIAL PRIMARY KEY,
    medication_a_id BIGINT NOT NULL REFERENCES medications(id),
    medication_b_id BIGINT NOT NULL REFERENCES medications(id),
    severity VARCHAR(20) NOT NULL,
    description TEXT,
    source VARCHAR(20) NOT NULL DEFAULT 'LOCAL',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_different_medications CHECK (medication_a_id != medication_b_id)
);

CREATE INDEX idx_interactions_med_a ON interactions(medication_a_id);
CREATE INDEX idx_interactions_med_b ON interactions(medication_b_id);
