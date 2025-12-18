-- PatientService extension tables for identity verification, feedback, triage, notification preference, navigation
-- Run after core schema to add supporting structures referenced in PatientService/API.md v2.0.0

BEGIN;

CREATE TABLE IF NOT EXISTS identity_verification (
    verification_id       VARCHAR(64) PRIMARY KEY,
    patient_id            VARCHAR(64) NOT NULL,
    full_name             VARCHAR(64) NOT NULL,
    id_number             VARCHAR(32) NOT NULL,
    id_front_image        TEXT        NOT NULL,
    id_back_image         TEXT        NOT NULL,
    face_snapshot         TEXT,
    status                VARCHAR(16) NOT NULL DEFAULT 'pending',
    submit_time           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    approved_time         TIMESTAMP WITH TIME ZONE,
    approved_by           VARCHAR(64),
    reason                TEXT,
    extra_payload         JSONB,
    CONSTRAINT fk_identity_patient FOREIGN KEY (patient_id) REFERENCES patient (id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_identity_patient ON identity_verification (patient_id);
CREATE INDEX IF NOT EXISTS idx_identity_status ON identity_verification (status);


CREATE TABLE IF NOT EXISTS registration_feedback (
    feedback_id      VARCHAR(64) PRIMARY KEY,
    registration_id  VARCHAR(64) NOT NULL UNIQUE,
    patient_id       VARCHAR(64) NOT NULL,
    score            INTEGER     NOT NULL CHECK (score BETWEEN 1 AND 5),
    tags             TEXT[],
    comment          TEXT,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_feedback_registration FOREIGN KEY (registration_id) REFERENCES register_record (id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_feedback_patient FOREIGN KEY (patient_id) REFERENCES patient (id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_feedback_patient ON registration_feedback (patient_id);


CREATE TABLE IF NOT EXISTS notification_preference (
    patient_id              VARCHAR(64) PRIMARY KEY,
    channels                TEXT[]      NOT NULL DEFAULT ARRAY['APP'],
    quiet_start             TIME,
    quiet_end               TIME,
    remind_before_minutes   INTEGER     DEFAULT 30,
    updated_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    extra_payload           JSONB,
    CONSTRAINT fk_notification_patient FOREIGN KEY (patient_id) REFERENCES patient (id) ON UPDATE CASCADE ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS triage_record (
    triage_id           VARCHAR(64) PRIMARY KEY,
    patient_id          VARCHAR(64) NOT NULL,
    symptoms            TEXT[]      NOT NULL,
    duration_days       INTEGER,
    temperature         NUMERIC(4,1),
    allergies           TEXT[],
    history             TEXT[],
    attachments         JSONB,
    recommendations     JSONB       NOT NULL,
    suggested_actions   TEXT[]      NOT NULL,
    mode                VARCHAR(16) NOT NULL DEFAULT 'hybrid',
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_triage_patient FOREIGN KEY (patient_id) REFERENCES patient (id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_triage_patient ON triage_record (patient_id);
CREATE INDEX IF NOT EXISTS idx_triage_mode ON triage_record (mode);


CREATE TABLE IF NOT EXISTS clinic_location (
    clinic_id       VARCHAR(64) PRIMARY KEY,
    campus_id       VARCHAR(64) NOT NULL,
    campus_name     VARCHAR(128) NOT NULL,
    floor           VARCHAR(16),
    latitude        DOUBLE PRECISION,
    longitude       DOUBLE PRECISION,
    indoor_lat      DOUBLE PRECISION,
    indoor_lng      DOUBLE PRECISION,
    address         VARCHAR(256),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_location_clinic FOREIGN KEY (clinic_id) REFERENCES clinic (id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_location_campus ON clinic_location (campus_id);

COMMIT;
