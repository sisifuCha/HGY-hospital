/*==============================================================*/
/* DBMS name:      PostgreSQL 17                                */
/* Created on:     2025/10/22                                   */
/* Description:    Hospital Registration System Database        */
/*==============================================================*/

-- Drop tables if they exist (PostgreSQL 17 compatible)
DROP TABLE IF EXISTS add_number_source_record CASCADE;
DROP TABLE IF EXISTS alternate_record CASCADE;
DROP TABLE IF EXISTS blacklist CASCADE;
DROP TABLE IF EXISTS cancel_record CASCADE;
DROP TABLE IF EXISTS clinic CASCADE;
DROP TABLE IF EXISTS department CASCADE;
DROP TABLE IF EXISTS doc_schedule_change_record CASCADE;
DROP TABLE IF EXISTS doc_schedule_record CASCADE;
DROP TABLE IF EXISTS doctor CASCADE;
DROP TABLE IF EXISTS medical_insurance CASCADE;
DROP TABLE IF EXISTS patient CASCADE;
DROP TABLE IF EXISTS pay_record CASCADE;
DROP TABLE IF EXISTS register_record CASCADE;
DROP TABLE IF EXISTS reimburse_type CASCADE;
DROP TABLE IF EXISTS schedule_template CASCADE;
DROP TABLE IF EXISTS sensitive_operation CASCADE;
DROP TABLE IF EXISTS title_number_source CASCADE;
DROP TABLE IF EXISTS "user" CASCADE;

/*==============================================================*/
/* Table: user (基础用户表)                                      */
/*==============================================================*/
CREATE TABLE "user" (
   ID                   VARCHAR(20)          NOT NULL,
   email                VARCHAR(100)         NULL,
   pass                 VARCHAR(255)         NULL,  -- 增加长度以支持加密密码
   name                 VARCHAR(50)          NULL,  -- 增加长度
   account              VARCHAR(50)          NULL,
   sex                  VARCHAR(10)          NULL,
   phone_num            VARCHAR(20)          NULL,  -- 增加长度以支持国际号码
   CONSTRAINT PK_USER PRIMARY KEY (ID)
);

COMMENT ON TABLE "user" IS '用户基础信息表';
COMMENT ON COLUMN "user".ID IS '用户唯一标识';
COMMENT ON COLUMN "user".email IS '邮箱地址';
COMMENT ON COLUMN "user".pass IS '密码(加密)';

/*==============================================================*/
/* Table: department (科室表)                                    */
/*==============================================================*/
CREATE TABLE department (
   ID                   VARCHAR(20)          NOT NULL,
   name                 VARCHAR(100)         NULL,
   father_ID            VARCHAR(20)          NULL,
   CONSTRAINT PK_DEPARTMENT PRIMARY KEY (ID)
);

COMMENT ON TABLE department IS '医院科室表';
COMMENT ON COLUMN department.father_ID IS '父级科室ID,支持科室层级结构';

/*==============================================================*/
/* Table: clinic (诊室表)                                        */
/*==============================================================*/
CREATE TABLE clinic (
   ID                   VARCHAR(20)          NOT NULL,
   clinic_number        VARCHAR(20)          NULL,
   location             VARCHAR(100)         NULL,
   dep_ID               VARCHAR(20)          NULL,
   CONSTRAINT PK_CLINIC PRIMARY KEY (ID)
);

COMMENT ON TABLE clinic IS '诊室信息表';

/*==============================================================*/
/* Table: title_number_source (职称号源表)                       */
/*==============================================================*/
CREATE TABLE title_number_source (
   ID                   VARCHAR(20)          NOT NULL,
   number_source_count  SMALLINT             NULL,
   ori_cost             NUMERIC(10,2)        NULL,  -- 使用 NUMERIC 替代 FLOAT8
   CONSTRAINT PK_TITLE_NUMBER_SOURCE PRIMARY KEY (ID),
   CONSTRAINT CHK_number_source_count CHECK (number_source_count >= 0)
);

COMMENT ON TABLE title_number_source IS '医生职称号源配置表';

/*==============================================================*/
/* Table: doctor (医生表)                                        */
/*==============================================================*/
CREATE TABLE doctor (
   ID                   VARCHAR(20)          NOT NULL,
   doc_title_ID         VARCHAR(20)          NULL,
   status               VARCHAR(20)          NULL,
   clinic_ID            VARCHAR(20)          NULL,
   CONSTRAINT PK_DOCTOR PRIMARY KEY (ID)
);

COMMENT ON TABLE doctor IS '医生信息表';
COMMENT ON COLUMN doctor.status IS '医生状态: 在职/休假/停诊等';

/*==============================================================*/
/* Table: schedule_template (排班模板表)                         */
/*==============================================================*/
CREATE TABLE schedule_template (
   id                   VARCHAR(20)          NOT NULL,
   start_time           TIME                 NULL,
   end_time             TIME                 NULL,
   clin_ID              VARCHAR(20)          NULL,
   time_period_name     VARCHAR(20)          NULL,
   CONSTRAINT PK_SCHEDULE_TEMPLATE PRIMARY KEY (id),
   CONSTRAINT CHK_time_range CHECK (end_time > start_time)
);

COMMENT ON TABLE schedule_template IS '医生排班时间模板';
COMMENT ON COLUMN schedule_template.time_period_name IS '时段名称: 上午/下午/晚上';

/*==============================================================*/
/* Table: doc_schedule_record (医生排班记录表)                   */
/*==============================================================*/
CREATE TABLE doc_schedule_record (
   ID                   VARCHAR(20)          NOT NULL,
   template_ID          VARCHAR(20)          NULL,
   schedule_date        DATE                 NULL,
   left_source_count    INTEGER              NULL,
   doc_ID               VARCHAR(20)          NULL,
   CONSTRAINT PK_DOC_SCHEDULE_RECORD PRIMARY KEY (ID),
   CONSTRAINT CHK_left_source_count CHECK (left_source_count >= 0)
);

COMMENT ON TABLE doc_schedule_record IS '医生实际排班记录表';
COMMENT ON COLUMN doc_schedule_record.left_source_count IS '剩余号源数量';

/*==============================================================*/
/* Table: reimburse_type (报销类型表)                            */
/*==============================================================*/
CREATE TABLE reimburse_type (
   ID                   VARCHAR(20)          NOT NULL,
   type                 VARCHAR(50)          NULL,
   percent              NUMERIC(5,2)         NULL,
   CONSTRAINT PK_REIMBURSE_TYPE PRIMARY KEY (ID),
   CONSTRAINT CHK_percent CHECK (percent >= 0 AND percent <= 100)
);

COMMENT ON TABLE reimburse_type IS '报销类型配置表';
COMMENT ON COLUMN reimburse_type.percent IS '报销比例(百分比)';

/*==============================================================*/
/* Table: medical_insurance (医保表)                             */
/*==============================================================*/
CREATE TABLE medical_insurance (
   ID                   VARCHAR(20)          NOT NULL,
   overage              NUMERIC(10,2)        NULL,
   CONSTRAINT PK_MEDICAL_INSURANCE PRIMARY KEY (ID),
   CONSTRAINT CHK_overage CHECK (overage >= 0)
);

COMMENT ON TABLE medical_insurance IS '医保账户信息表';
COMMENT ON COLUMN medical_insurance.overage IS '医保余额';

/*==============================================================*/
/* Table: patient (患者表)                                       */
/*==============================================================*/
CREATE TABLE patient (
   ID                   VARCHAR(20)          NOT NULL,
   birth                DATE                 NULL,
   ID_num               VARCHAR(18)          NULL,
   medical_insuranceID  VARCHAR(20)          NULL,
   reimburse_ID         VARCHAR(20)          NULL,
   CONSTRAINT PK_PATIENT PRIMARY KEY (ID),
   CONSTRAINT CHK_birth CHECK (birth <= CURRENT_DATE)
);

COMMENT ON TABLE patient IS '患者信息表';
COMMENT ON COLUMN patient.ID_num IS '身份证号';

/*==============================================================*/
/* Table: register_record (挂号记录表)                           */
/*==============================================================*/
CREATE TABLE register_record (
   patient_ID           VARCHAR(20)          NOT NULL,  -- 修正拼写错误
   sch_ID               VARCHAR(20)          NOT NULL,
   register_time        TIMESTAMP            NULL,
   status               VARCHAR(20)          NULL,
   CONSTRAINT PK_REGISTER_RECORD PRIMARY KEY (patient_ID, sch_ID)
);

COMMENT ON TABLE register_record IS '挂号记录表';
COMMENT ON COLUMN register_record.status IS '挂号状态: 已预约/已就诊/已取消等';

/*==============================================================*/
/* Table: alternate_record (候补记录表)                          */
/*==============================================================*/
CREATE TABLE alternate_record (
   patient_ID           VARCHAR(20)          NOT NULL,  -- 修正拼写错误
   sch_ID               VARCHAR(20)          NOT NULL,
   register_time        TIMESTAMP            NULL,
   status               VARCHAR(20)          NULL,
   CONSTRAINT PK_ALTERNATE_RECORD PRIMARY KEY (patient_ID, sch_ID)
);

COMMENT ON TABLE alternate_record IS '候补挂号记录表';

/*==============================================================*/
/* Table: add_number_source_record (加号申请记录表)              */
/*==============================================================*/
CREATE TABLE add_number_source_record (
   patient_ID           VARCHAR(20)          NOT NULL,
   sch_ID               VARCHAR(20)          NOT NULL,
   apply_time           TIMESTAMP            NULL,
   status               VARCHAR(20)          NULL,
   reason_text          TEXT                 NULL,  -- 使用 TEXT 替代 VARCHAR(200)
   reason_pic           VARCHAR(255)         NULL,
   CONSTRAINT PK_ADD_NUMBER_SOURCE_RECORD PRIMARY KEY (patient_ID, sch_ID)
);

COMMENT ON TABLE add_number_source_record IS '加号申请记录表';
COMMENT ON COLUMN add_number_source_record.reason_pic IS '申请理由图片路径';

/*==============================================================*/
/* Table: cancel_record (取消挂号记录表)                         */
/*==============================================================*/
CREATE TABLE cancel_record (
   patient_ID           VARCHAR(20)          NOT NULL,
   sch_ID               VARCHAR(20)          NOT NULL,
   cancel_time          TIMESTAMP            NULL,
   reason_text          TEXT                 NULL,
   reason_pic           VARCHAR(255)         NULL,
   CONSTRAINT PK_CANCEL_RECORD PRIMARY KEY (patient_ID, sch_ID)
);

COMMENT ON TABLE cancel_record IS '取消挂号记录表';

/*==============================================================*/
/* Table: doc_schedule_change_record (医生排班变更记录表)        */
/*==============================================================*/
CREATE TABLE doc_schedule_change_record (
   doc_ID               VARCHAR(20)          NOT NULL,
   ori_sch_ID           VARCHAR(20)          NOT NULL,
   target_sch_ID        VARCHAR(20)          NOT NULL,
   reason_text          TEXT                 NULL,
   status               VARCHAR(20)          NULL,
   CONSTRAINT PK_DOC_SCHEDULE_CHANGE_RECORD PRIMARY KEY (doc_ID, ori_sch_ID, target_sch_ID)
);

COMMENT ON TABLE doc_schedule_change_record IS '医生排班变更记录表';

/*==============================================================*/
/* Table: pay_record (支付记录表)                                */
/*==============================================================*/
CREATE TABLE pay_record (
   id                   VARCHAR(100)         NOT NULL,
   pay_time             TIMESTAMP            NULL,
   pay_status           VARCHAR(20)          NULL,
   ori_amount           NUMERIC(10,2)        NULL,
   ask_pay_amount       NUMERIC(10,2)        NULL,
   patient_ID           VARCHAR(20)          NULL,
   doc_ID               VARCHAR(20)          NULL,
   CONSTRAINT PK_PAY_RECORD PRIMARY KEY (id),
   CONSTRAINT CHK_amounts CHECK (ori_amount >= 0 AND ask_pay_amount >= 0)
);

COMMENT ON TABLE pay_record IS '支付记录表';
COMMENT ON COLUMN pay_record.ori_amount IS '原始金额';
COMMENT ON COLUMN pay_record.ask_pay_amount IS '实际支付金额';

/*==============================================================*/
/* Table: sensitive_operation (敏感操作记录表)                   */
/*==============================================================*/
CREATE TABLE sensitive_operation (
   ID                   VARCHAR(20)          NOT NULL,
   patient_ID           VARCHAR(20)          NULL,
   sensitive_op_type    VARCHAR(50)          NULL,
   op_time              TIMESTAMP            NULL,
   Remark               TEXT                 NULL,
   CONSTRAINT PK_SENSITIVE_OPERATION PRIMARY KEY (ID)
);

COMMENT ON TABLE sensitive_operation IS '敏感操作记录表';
COMMENT ON COLUMN sensitive_operation.sensitive_op_type IS '操作类型: 频繁取消/爽约等';

/*==============================================================*/
/* Table: blacklist (黑名单表)                                   */
/*==============================================================*/
CREATE TABLE blacklist (
   id                   VARCHAR(20)          NOT NULL,
   sen_ID1              VARCHAR(20)          NULL,
   sen_ID2              VARCHAR(20)          NULL,
   sen_ID3              VARCHAR(20)          NULL,
   count                SMALLINT             NULL,
   CONSTRAINT PK_BLACKLIST PRIMARY KEY (id),
   CONSTRAINT CHK_count CHECK (count >= 0)
);

COMMENT ON TABLE blacklist IS '黑名单表';
COMMENT ON COLUMN blacklist.count IS '违规次数';

/*==============================================================*/
/* Foreign Key Constraints                                      */
/*==============================================================*/

-- Department self-reference
ALTER TABLE department
   ADD CONSTRAINT FK_DEPARTMENT_PARENT
   FOREIGN KEY (father_ID)
   REFERENCES department (ID)
   ON DELETE SET NULL
   ON UPDATE CASCADE;

-- Clinic references
ALTER TABLE clinic
   ADD CONSTRAINT FK_CLINIC_DEPARTMENT
   FOREIGN KEY (dep_ID)
   REFERENCES department (ID)
   ON DELETE SET NULL
   ON UPDATE CASCADE;

-- Doctor references
ALTER TABLE doctor
   ADD CONSTRAINT FK_DOCTOR_USER
   FOREIGN KEY (ID)
   REFERENCES "user" (ID)
   ON DELETE CASCADE
   ON UPDATE CASCADE;

ALTER TABLE doctor
   ADD CONSTRAINT FK_DOCTOR_CLINIC
   FOREIGN KEY (clinic_ID)
   REFERENCES clinic (ID)
   ON DELETE SET NULL
   ON UPDATE CASCADE;

ALTER TABLE doctor
   ADD CONSTRAINT FK_DOCTOR_TITLE
   FOREIGN KEY (doc_title_ID)
   REFERENCES title_number_source (ID)
   ON DELETE SET NULL
   ON UPDATE CASCADE;

-- Schedule template references
ALTER TABLE schedule_template
   ADD CONSTRAINT FK_SCHEDULE_TEMPLATE_CLINIC
   FOREIGN KEY (clin_ID)
   REFERENCES clinic (ID)
   ON DELETE SET NULL
   ON UPDATE CASCADE;

-- Doc schedule record references
ALTER TABLE doc_schedule_record
   ADD CONSTRAINT FK_DOC_SCHEDULE_TEMPLATE
   FOREIGN KEY (template_ID)
   REFERENCES schedule_template (id)
   ON DELETE SET NULL
   ON UPDATE CASCADE;

ALTER TABLE doc_schedule_record
   ADD CONSTRAINT FK_DOC_SCHEDULE_DOCTOR
   FOREIGN KEY (doc_ID)
   REFERENCES doctor (ID)
   ON DELETE CASCADE
   ON UPDATE CASCADE;

-- Patient references
ALTER TABLE patient
   ADD CONSTRAINT FK_PATIENT_USER
   FOREIGN KEY (ID)
   REFERENCES "user" (ID)
   ON DELETE CASCADE
   ON UPDATE CASCADE;

ALTER TABLE patient
   ADD CONSTRAINT FK_PATIENT_REIMBURSE
   FOREIGN KEY (reimburse_ID)
   REFERENCES reimburse_type (ID)
   ON DELETE SET NULL
   ON UPDATE CASCADE;

ALTER TABLE patient
   ADD CONSTRAINT FK_PATIENT_MEDICAL_INSURANCE
   FOREIGN KEY (medical_insuranceID)
   REFERENCES medical_insurance (ID)
   ON DELETE SET NULL
   ON UPDATE CASCADE;

-- Register record references
ALTER TABLE register_record
   ADD CONSTRAINT FK_REGISTER_SCHEDULE
   FOREIGN KEY (sch_ID)
   REFERENCES doc_schedule_record (ID)
   ON DELETE CASCADE
   ON UPDATE CASCADE;

ALTER TABLE register_record
   ADD CONSTRAINT FK_REGISTER_PATIENT
   FOREIGN KEY (patient_ID)
   REFERENCES patient (ID)
   ON DELETE CASCADE
   ON UPDATE CASCADE;

-- Alternate record references
ALTER TABLE alternate_record
   ADD CONSTRAINT FK_ALTERNATE_PATIENT
   FOREIGN KEY (patient_ID)
   REFERENCES patient (ID)
   ON DELETE CASCADE
   ON UPDATE CASCADE;

ALTER TABLE alternate_record
   ADD CONSTRAINT FK_ALTERNATE_SCHEDULE
   FOREIGN KEY (sch_ID)
   REFERENCES doc_schedule_record (ID)
   ON DELETE CASCADE
   ON UPDATE CASCADE;

-- Add number source record references
ALTER TABLE add_number_source_record
   ADD CONSTRAINT FK_ADD_NUMBER_SCHEDULE
   FOREIGN KEY (sch_ID)
   REFERENCES doc_schedule_record (ID)
   ON DELETE CASCADE
   ON UPDATE CASCADE;

ALTER TABLE add_number_source_record
   ADD CONSTRAINT FK_ADD_NUMBER_PATIENT
   FOREIGN KEY (patient_ID)
   REFERENCES patient (ID)
   ON DELETE CASCADE
   ON UPDATE CASCADE;

-- Cancel record references
ALTER TABLE cancel_record
   ADD CONSTRAINT FK_CANCEL_SCHEDULE
   FOREIGN KEY (sch_ID)
   REFERENCES doc_schedule_record (ID)
   ON DELETE CASCADE
   ON UPDATE CASCADE;

ALTER TABLE cancel_record
   ADD CONSTRAINT FK_CANCEL_PATIENT
   FOREIGN KEY (patient_ID)
   REFERENCES patient (ID)
   ON DELETE CASCADE
   ON UPDATE CASCADE;

-- Doc schedule change record references
ALTER TABLE doc_schedule_change_record
   ADD CONSTRAINT FK_DOC_CHANGE_DOCTOR
   FOREIGN KEY (doc_ID)
   REFERENCES doctor (ID)
   ON DELETE CASCADE
   ON UPDATE CASCADE;

ALTER TABLE doc_schedule_change_record
   ADD CONSTRAINT FK_DOC_CHANGE_ORI_SCHEDULE
   FOREIGN KEY (ori_sch_ID)
   REFERENCES doc_schedule_record (ID)
   ON DELETE CASCADE
   ON UPDATE CASCADE;

ALTER TABLE doc_schedule_change_record
   ADD CONSTRAINT FK_DOC_CHANGE_TARGET_SCHEDULE
   FOREIGN KEY (target_sch_ID)
   REFERENCES doc_schedule_record (ID)
   ON DELETE CASCADE
   ON UPDATE CASCADE;

-- Pay record references
ALTER TABLE pay_record
   ADD CONSTRAINT FK_PAY_PATIENT
   FOREIGN KEY (patient_ID)
   REFERENCES patient (ID)
   ON DELETE SET NULL
   ON UPDATE CASCADE;

ALTER TABLE pay_record
   ADD CONSTRAINT FK_PAY_SCHEDULE
   FOREIGN KEY (doc_ID)
   REFERENCES doc_schedule_record (ID)
   ON DELETE SET NULL
   ON UPDATE CASCADE;

-- Sensitive operation references
ALTER TABLE sensitive_operation
   ADD CONSTRAINT FK_SENSITIVE_PATIENT
   FOREIGN KEY (patient_ID)
   REFERENCES patient (ID)
   ON DELETE CASCADE
   ON UPDATE CASCADE;

-- Blacklist references
ALTER TABLE blacklist
   ADD CONSTRAINT FK_BLACKLIST_SEN1
   FOREIGN KEY (sen_ID1)
   REFERENCES sensitive_operation (ID)
   ON DELETE SET NULL
   ON UPDATE CASCADE;

ALTER TABLE blacklist
   ADD CONSTRAINT FK_BLACKLIST_SEN2
   FOREIGN KEY (sen_ID2)
   REFERENCES sensitive_operation (ID)
   ON DELETE SET NULL
   ON UPDATE CASCADE;

ALTER TABLE blacklist
   ADD CONSTRAINT FK_BLACKLIST_SEN3
   FOREIGN KEY (sen_ID3)
   REFERENCES sensitive_operation (ID)
   ON DELETE SET NULL
   ON UPDATE CASCADE;

/*==============================================================*/
/* Indexes for Performance Optimization                         */
/*==============================================================*/

-- User indexes
CREATE INDEX idx_user_email ON "user"(email);
CREATE INDEX idx_user_account ON "user"(account);
CREATE INDEX idx_user_phone ON "user"(phone_num);

-- Department indexes
CREATE INDEX idx_department_father ON department(father_ID);
CREATE INDEX idx_department_name ON department(name);

-- Clinic indexes
CREATE INDEX idx_clinic_department ON clinic(dep_ID);

-- Doctor indexes
CREATE INDEX idx_doctor_clinic ON doctor(clinic_ID);
CREATE INDEX idx_doctor_title ON doctor(doc_title_ID);
CREATE INDEX idx_doctor_status ON doctor(status);

-- Schedule indexes
CREATE INDEX idx_schedule_record_date ON doc_schedule_record(schedule_date);
CREATE INDEX idx_schedule_record_doctor ON doc_schedule_record(doc_ID);
CREATE INDEX idx_schedule_template_clinic ON schedule_template(clin_ID);

-- Patient indexes
CREATE INDEX idx_patient_id_num ON patient(ID_num);
CREATE INDEX idx_patient_medical_insurance ON patient(medical_insuranceID);

-- Register record indexes
CREATE INDEX idx_register_time ON register_record(register_time);
CREATE INDEX idx_register_status ON register_record(status);
CREATE INDEX idx_register_patient ON register_record(patient_ID);
CREATE INDEX idx_register_schedule ON register_record(sch_ID);

-- Pay record indexes
CREATE INDEX idx_pay_time ON pay_record(pay_time);
CREATE INDEX idx_pay_status ON pay_record(pay_status);
CREATE INDEX idx_pay_patient ON pay_record(patient_ID);

-- Sensitive operation indexes
CREATE INDEX idx_sensitive_patient ON sensitive_operation(patient_ID);
CREATE INDEX idx_sensitive_time ON sensitive_operation(op_time);
CREATE INDEX idx_sensitive_type ON sensitive_operation(sensitive_op_type);

/*==============================================================*/
/* Views for Common Queries                                     */
/*==============================================================*/

-- 可用号源视图
CREATE OR REPLACE VIEW v_available_schedules AS
SELECT
    dsr.ID as schedule_id,
    dsr.schedule_date,
    dsr.left_source_count,
    d.ID as doctor_id,
    u.name as doctor_name,
    st.time_period_name,
    st.start_time,
    st.end_time,
    c.clinic_number,
    c.location,
    dep.name as department_name,
    tns.ori_cost
FROM doc_schedule_record dsr
JOIN doctor d ON dsr.doc_ID = d.ID
JOIN "user" u ON d.ID = u.ID
JOIN schedule_template st ON dsr.template_ID = st.id
JOIN clinic c ON d.clinic_ID = c.ID
JOIN department dep ON c.dep_ID = dep.ID
JOIN title_number_source tns ON d.doc_title_ID = tns.ID
WHERE dsr.left_source_count > 0
  AND dsr.schedule_date >= CURRENT_DATE;

COMMENT ON VIEW v_available_schedules IS '可用号源视图';

-- 患者挂号历史视图
CREATE OR REPLACE VIEW v_patient_register_history AS
SELECT
    p.ID as patient_id,
    u.name as patient_name,
    u.phone_num,
    rr.register_time,
    rr.status,
    dsr.schedule_date,
    doc_u.name as doctor_name,
    dep.name as department_name,
    c.clinic_number
FROM register_record rr
JOIN patient p ON rr.patient_ID = p.ID
JOIN "user" u ON p.ID = u.ID
JOIN doc_schedule_record dsr ON rr.sch_ID = dsr.ID
JOIN doctor d ON dsr.doc_ID = d.ID
JOIN "user" doc_u ON d.ID = doc_u.ID
JOIN clinic c ON d.clinic_ID = c.ID
JOIN department dep ON c.dep_ID = dep.ID;

COMMENT ON VIEW v_patient_register_history IS '患者挂号历史视图';

/*==============================================================*/
/* Initial Data / Sample Data (Optional)                       */
/*==============================================================*/

-- 插入示例报销类型
INSERT INTO reimburse_type (ID, type, percent) VALUES
('RT001', '城镇职工医保', 80.00),
('RT002', '城乡居民医保', 70.00),
('RT003', '自费', 0.00)
ON CONFLICT (ID) DO NOTHING;

COMMENT ON DATABASE postgres IS 'Hospital Registration System - PostgreSQL 17 Compatible';
