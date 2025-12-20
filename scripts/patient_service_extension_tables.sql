-- PatientService 扩展表/扩展字段脚本（建议在 hospital_register_postgresql_17.sql 之后执行）
-- 目标：补齐患者端 v2.0.0 所需的“档案扩展字段、实名认证、评价、导诊、导航、消息偏好”等持久化结构。

BEGIN;

/* ------------------------------------------------------------
 * 1) 扩展 patient 表：补齐档案字段（用于 /api/patients/{id} 与 /api/patients/{id}/profile 等）
 *   - phone：患者档案电话（展示优先级高于 user.phone_num）
 *   - address/medical_history/allergies：档案扩展
 * 说明：基础脚本 patient 表 column 为 id/birth/ID_num/medical_insuranceID/reimburse_ID
 * ------------------------------------------------------------ */
ALTER TABLE patient
    ADD COLUMN IF NOT EXISTS phone VARCHAR(20),
    ADD COLUMN IF NOT EXISTS address VARCHAR(255),
    ADD COLUMN IF NOT EXISTS medical_history TEXT,
    ADD COLUMN IF NOT EXISTS allergies TEXT;

CREATE INDEX IF NOT EXISTS idx_patient_phone ON patient(phone);

/* ------------------------------------------------------------
 * 2) 实名认证记录表
 * ------------------------------------------------------------ */
CREATE TABLE IF NOT EXISTS identity_verification (
    id              BIGSERIAL PRIMARY KEY,
    patient_id      VARCHAR(20) NOT NULL,
    real_name       VARCHAR(50) NOT NULL,
    id_number       VARCHAR(32) NOT NULL,
    id_type         VARCHAR(20) NOT NULL DEFAULT 'CN_ID',
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    verified_at     TIMESTAMP NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_identity_patient FOREIGN KEY (patient_id) REFERENCES "user"(id)
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_identity_patient ON identity_verification(patient_id);
CREATE INDEX IF NOT EXISTS idx_identity_status ON identity_verification(status);

/* ------------------------------------------------------------
 * 3) 就诊评价（与挂号记录关联）
 * register_record 目前主键是 (patient_ID, sch_ID)，这里用同样键关联
 * ------------------------------------------------------------ */
CREATE TABLE IF NOT EXISTS registration_feedback (
    id              BIGSERIAL PRIMARY KEY,
    patient_id      VARCHAR(20) NOT NULL,
    sch_id          VARCHAR(20) NOT NULL,
    rating          SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    content         TEXT NULL,
    tags            TEXT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_feedback_register FOREIGN KEY (patient_id, sch_id)
        REFERENCES register_record(patient_ID, sch_ID)
        ON DELETE CASCADE
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_feedback_register ON registration_feedback(patient_id, sch_id);
CREATE INDEX IF NOT EXISTS idx_feedback_patient ON registration_feedback(patient_id);

/* ------------------------------------------------------------
 * 4) 消息偏好（可用于消息提醒开关）
 * ------------------------------------------------------------ */
CREATE TABLE IF NOT EXISTS notification_preference (
    patient_id          VARCHAR(20) PRIMARY KEY,
    enable_register     BOOLEAN NOT NULL DEFAULT TRUE,
    enable_cancel       BOOLEAN NOT NULL DEFAULT TRUE,
    enable_payment      BOOLEAN NOT NULL DEFAULT TRUE,
    enable_waiting      BOOLEAN NOT NULL DEFAULT TRUE,
    enable_system       BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_pref_patient FOREIGN KEY (patient_id) REFERENCES "user"(id)
);

/* ------------------------------------------------------------
 * 5) 智能导诊记录（最小可用：症状->建议科室/动作，建议结果用 JSON 文本落库）
 * ------------------------------------------------------------ */
CREATE TABLE IF NOT EXISTS triage_record (
    id              BIGSERIAL PRIMARY KEY,
    patient_id      VARCHAR(20) NOT NULL,
    symptoms        TEXT NOT NULL,
    duration_days   INTEGER NULL,
    temperature     NUMERIC(5,2) NULL,
    allergies       TEXT NULL,
    history         TEXT NULL,
    mode            VARCHAR(20) NOT NULL DEFAULT 'RULE',
    recommendations_json TEXT NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_triage_patient FOREIGN KEY (patient_id) REFERENCES "user"(id)
);
CREATE INDEX IF NOT EXISTS idx_triage_patient_time ON triage_record(patient_id, created_at DESC);

/* ------------------------------------------------------------
 * 6) 诊室/楼宇地理位置（导航）
 * 关联 clinic.id
 * ------------------------------------------------------------ */
CREATE TABLE IF NOT EXISTS clinic_location (
    clinic_id       VARCHAR(20) PRIMARY KEY,
    building        VARCHAR(50) NULL,
    floor           VARCHAR(20) NULL,
    room            VARCHAR(50) NULL,
    lat             NUMERIC(10,7) NULL,
    lng             NUMERIC(10,7) NULL,
    navigation_text TEXT NULL,
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_location_clinic FOREIGN KEY (clinic_id) REFERENCES clinic(id)
);
CREATE INDEX IF NOT EXISTS idx_clinic_location_latlng ON clinic_location(lat, lng);

COMMIT;

