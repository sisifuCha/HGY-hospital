-- "user" definition

-- Drop table

-- DROP TABLE "user";

CREATE TABLE "user" (
    id varchar(20) NOT NULL,
    email varchar(100) NULL,
    pass varchar(255) NULL,
    name varchar(50) NULL,
    account varchar(50) NULL,
    sex varchar(10) NULL,
    phone_num varchar(20) NULL,
    user_type varchar(3) NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY (id)
);

CREATE INDEX idx_user_account ON "user" USING btree (account);

CREATE INDEX idx_user_email ON "user" USING btree (email);

CREATE INDEX idx_user_phone ON "user" USING btree (phone_num);

-- title_number_source definition

-- Drop table

-- DROP TABLE title_number_source;

CREATE TABLE title_number_source (
    id varchar(20) NOT NULL,
    "name" varchar(10) NOT NULL,
    number_source_count int2 NULL,
    ori_cost numeric(10, 2) NULL,
    CONSTRAINT chk_number_source_count CHECK ((number_source_count >= 0)),
    CONSTRAINT pk_title_number_source PRIMARY KEY (id)
);

-- sensitive_operation definition

-- Drop table

-- DROP TABLE sensitive_operation;

CREATE TABLE sensitive_operation (
    id varchar(20) NOT NULL,
    patient_id varchar(20) NULL,
    sensitive_op_type varchar(50) NULL,
    op_time timestamp NULL,
    remark text NULL,
    CONSTRAINT pk_sensitive_operation PRIMARY KEY (id),
    CONSTRAINT fk_sensitive_patient FOREIGN KEY (patient_id) REFERENCES patient (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX idx_sensitive_patient ON sensitive_operation USING btree (patient_id);

CREATE INDEX idx_sensitive_time ON sensitive_operation USING btree (op_time);

CREATE INDEX idx_sensitive_type ON sensitive_operation USING btree (sensitive_op_type);

-- schedule_template definition

-- Drop table

-- DROP TABLE schedule_template;

CREATE TABLE schedule_template (
    id varchar(20) NOT NULL,
    start_time time NULL,
    end_time time NULL,
    clin_id varchar(20) NULL,
    time_period_name varchar(20) NULL,
    CONSTRAINT pk_schedule_template PRIMARY KEY (id),
    CONSTRAINT fk_schedule_template_clinic FOREIGN KEY (clin_id) REFERENCES clinic (id) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE INDEX idx_schedule_template_clinic ON schedule_template USING btree (clin_id);

-- reimburse_type definition

-- Drop table

-- DROP TABLE reimburse_type;

CREATE TABLE reimburse_type (
    id varchar(20) NOT NULL,
    "type" varchar(50) NULL,
    "percent" numeric(5, 2) NULL,
    CONSTRAINT chk_percent CHECK (
        (
            (percent >= (0)::numeric)
            AND (percent <= (100)::numeric)
        )
    ),
    CONSTRAINT pk_reimburse_type PRIMARY KEY (id)
);

-- register_record definition

-- Drop table

-- DROP TABLE register_record;

CREATE TABLE register_record (
    patient_id varchar(20) NOT NULL,
    sch_id varchar(20) NOT NULL,
    register_time timestamp NULL,
    status varchar(20) NULL,
    CONSTRAINT pk_register_record PRIMARY KEY (patient_id, sch_id),
    CONSTRAINT fk_register_patient FOREIGN KEY (patient_id) REFERENCES patient (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_register_schedule FOREIGN KEY (sch_id) REFERENCES doc_schedule_record (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX idx_register_patient ON register_record USING btree (patient_id);

CREATE INDEX idx_register_schedule ON register_record USING btree (sch_id);

CREATE INDEX idx_register_status ON register_record USING btree (status);

CREATE INDEX idx_register_time ON register_record USING btree (register_time);

-- pay_record definition

-- Drop table

-- DROP TABLE pay_record;

CREATE TABLE pay_record (
    id varchar(100) NOT NULL,
    pay_time timestamp NULL,
    pay_status varchar(20) NULL,
    ori_amount numeric(10, 2) NULL,
    ask_pay_amount numeric(10, 2) NULL,
    patient_id varchar(20) NULL,
    sch_id varchar(20) NULL,
    CONSTRAINT chk_amounts CHECK (
        (
            (ori_amount >= (0)::numeric)
            AND (
                ask_pay_amount >= (0)::numeric
            )
        )
    ),
    CONSTRAINT pk_pay_record PRIMARY KEY (id),
    CONSTRAINT fk_pay_patient FOREIGN KEY (patient_id) REFERENCES patient (id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_pay_schedule FOREIGN KEY (sch_id) REFERENCES doc_schedule_record (id)
);

CREATE INDEX idx_pay_patient ON pay_record USING btree (patient_id);

CREATE INDEX idx_pay_status ON pay_record USING btree (pay_status);

CREATE INDEX idx_pay_time ON pay_record USING btree (pay_time);

-- patient definition

-- Drop table

-- DROP TABLE patient;

CREATE TABLE patient (
    id varchar(20) NOT NULL,
    birth date NULL,
    id_num varchar(18) NULL,
    medical_insuranceid varchar(20) NULL,
    reimburse_id varchar(20) NULL,
    CONSTRAINT chk_birth CHECK ((birth <= CURRENT_DATE)),
    CONSTRAINT pk_patient PRIMARY KEY (id),
    CONSTRAINT fk_patient_medical_insurance FOREIGN KEY (medical_insuranceid) REFERENCES medical_insurance (id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_patient_reimburse FOREIGN KEY (reimburse_id) REFERENCES reimburse_type (id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_patient_user FOREIGN KEY (id) REFERENCES "user" (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX idx_patient_id_num ON patient USING btree (id_num);

CREATE INDEX idx_patient_medical_insurance ON patient USING btree (medical_insuranceid);

-- message_record definition

-- Drop table

-- DROP TABLE message_record;

CREATE TABLE message_record (
    id bigserial NOT NULL,
    title varchar(200) NOT NULL,
    "content" text NOT NULL,
    sender_type varchar(20) NOT NULL,
    receiver_type varchar(30) NOT NULL,
    receiver_id varchar(20) NULL,
    status varchar(20) DEFAULT 'unsent'::character varying NOT NULL,
    read_status varchar(20) DEFAULT 'unconfirmed'::character varying NOT NULL,
    created_time timestamp DEFAULT CURRENT_TIMESTAMP NULL,
    updated_time timestamp DEFAULT CURRENT_TIMESTAMP NULL,
    over_time timestamp NULL,
    CONSTRAINT message_record_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_message_created_time ON message_record USING btree (created_time);

CREATE INDEX idx_message_over_time ON message_record USING btree (over_time);

CREATE INDEX idx_message_receiver_type_id ON message_record USING btree (receiver_type, receiver_id);

CREATE INDEX idx_message_status ON message_record USING btree (status);

-- Table Triggers

create trigger message_record_notify_trigger after
insert
    on
    message_record for each row execute function notify_message_change();

create trigger update_message_updated_time before
update
    on
    message_record for each row execute function update_updated_time_column();

-- medical_insurance definition

-- Drop table

-- DROP TABLE medical_insurance;

CREATE TABLE medical_insurance (
    id varchar(20) NOT NULL,
    overage numeric(10, 2) NULL,
    CONSTRAINT chk_overage CHECK ((overage >= (0)::numeric)),
    CONSTRAINT pk_medical_insurance PRIMARY KEY (id)
);

-- doctor definition

-- Drop table

-- DROP TABLE doctor;

CREATE TABLE doctor (
    id varchar(20) NOT NULL,
    doc_title_id varchar(20) NULL,
    status varchar(20) NULL,
    clinic_id varchar(20) NULL,
    details varchar(255) NULL,
    specialty varchar(255) NULL,
    depart_id varchar(20) NULL,
    CONSTRAINT pk_doctor PRIMARY KEY (id),
    CONSTRAINT fk_doctor_clinic FOREIGN KEY (clinic_id) REFERENCES clinic (id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_doctor_title FOREIGN KEY (doc_title_id) REFERENCES title_number_source (id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_doctor_user FOREIGN KEY (id) REFERENCES "user" (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX idx_doctor_clinic ON doctor USING btree (clinic_id);

CREATE INDEX idx_doctor_status ON doctor USING btree (status);

CREATE INDEX idx_doctor_title ON doctor USING btree (doc_title_id);

-- doc_schedule_record definition

-- Drop table

-- DROP TABLE doc_schedule_record;

CREATE TABLE doc_schedule_record (
    id varchar(20) NOT NULL,
    template_id varchar(20) NULL,
    schedule_date date NULL,
    left_source_count int4 NULL,
    doc_id varchar(20) NULL,
    clinic_id varchar(20) NULL,
    status int4 DEFAULT 0 NULL,
    reason varchar(50) NULL,
    CONSTRAINT chk_left_source_count CHECK ((left_source_count >= 0)),
    CONSTRAINT pk_doc_schedule_record PRIMARY KEY (id),
    CONSTRAINT fk_doc_schedule_doctor FOREIGN KEY (doc_id) REFERENCES doctor (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_doc_schedule_template FOREIGN KEY (template_id) REFERENCES schedule_template (id) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE INDEX idx_schedule_record_date ON doc_schedule_record USING btree (schedule_date);

CREATE INDEX idx_schedule_record_doctor ON doc_schedule_record USING btree (doc_id);

-- doc_schedule_change_record definition

-- Drop table

-- DROP TABLE doc_schedule_change_record;

CREATE TABLE doc_schedule_change_record (
    doc_id varchar(20) NOT NULL,
    ori_sch_id varchar(20) NOT NULL,
    reason_text text NULL,
    status varchar(20) NULL,
    target_sch_id varchar(20) NULL,
    target_date date NULL,
    template_id varchar(20) NULL,
    "type" int4 NULL,
    leave_time_length int4 NULL,
    CONSTRAINT doc_schedule_change_record_pkey PRIMARY KEY (ori_sch_id),
    CONSTRAINT fk_doc_change_doctor FOREIGN KEY (doc_id) REFERENCES doctor (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_doc_change_ori_schedule FOREIGN KEY (ori_sch_id) REFERENCES doc_schedule_record (id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- department definition

-- Drop table

-- DROP TABLE department;

CREATE TABLE department (
    id varchar(20) NOT NULL,
    "name" varchar(100) NULL,
    father_id varchar(20) NULL,
    CONSTRAINT pk_department PRIMARY KEY (id),
    CONSTRAINT fk_department_parent FOREIGN KEY (father_id) REFERENCES department (id) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE INDEX idx_department_father ON department USING btree (father_id);

CREATE INDEX idx_department_name ON department USING btree (name);

-- clinic definition

-- Drop table

-- DROP TABLE clinic;

CREATE TABLE clinic (
    id varchar(20) NOT NULL,
    clinic_number varchar(20) NULL,
    "location" varchar(100) NULL,
    dep_id varchar(20) NULL,
    CONSTRAINT pk_clinic PRIMARY KEY (id),
    CONSTRAINT fk_clinic_department FOREIGN KEY (dep_id) REFERENCES department (id) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE INDEX idx_clinic_department ON clinic USING btree (dep_id);

-- cancel_record definition

-- Drop table

-- DROP TABLE cancel_record;

CREATE TABLE cancel_record (
    patient_id varchar(20) NOT NULL,
    sch_id varchar(20) NOT NULL,
    cancel_time timestamp NULL,
    reason_text text NULL,
    reason_pic varchar(255) NULL,
    CONSTRAINT pk_cancel_record PRIMARY KEY (patient_id, sch_id),
    CONSTRAINT fk_cancel_patient FOREIGN KEY (patient_id) REFERENCES patient (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_cancel_schedule FOREIGN KEY (sch_id) REFERENCES doc_schedule_record (id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- blacklist definition

-- Drop table

-- DROP TABLE blacklist;

CREATE TABLE blacklist (
    id varchar(20) NOT NULL,
    sen_id1 varchar(20) NULL,
    sen_id2 varchar(20) NULL,
    sen_id3 varchar(20) NULL,
    count int2 NULL,
    CONSTRAINT chk_count CHECK ((count >= 0)),
    CONSTRAINT pk_blacklist PRIMARY KEY (id),
    CONSTRAINT fk_blacklist_sen1 FOREIGN KEY (sen_id1) REFERENCES sensitive_operation (id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_blacklist_sen2 FOREIGN KEY (sen_id2) REFERENCES sensitive_operation (id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_blacklist_sen3 FOREIGN KEY (sen_id3) REFERENCES sensitive_operation (id) ON DELETE SET NULL ON UPDATE CASCADE
);

-- alternate_record definition

-- Drop table

-- DROP TABLE alternate_record;

CREATE TABLE alternate_record (
    patient_id varchar(20) NOT NULL,
    sch_id varchar(20) NOT NULL,
    register_time timestamp NULL,
    status varchar(20) NULL,
    CONSTRAINT pk_alternate_record PRIMARY KEY (patient_id, sch_id),
    CONSTRAINT fk_alternate_patient FOREIGN KEY (patient_id) REFERENCES patient (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_alternate_schedule FOREIGN KEY (sch_id) REFERENCES doc_schedule_record (id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- add_number_source_record definition

-- Drop table

-- DROP TABLE add_number_source_record;

CREATE TABLE add_number_source_record (
    patient_id varchar(20) NOT NULL,
    sch_id varchar(20) NOT NULL,
    apply_time timestamp NULL,
    status varchar(20) DEFAULT '待审核'::character varying NULL,
    reason_text text NULL,
    reason_pic varchar(255) NULL,
    CONSTRAINT pk_add_number_source_record PRIMARY KEY (patient_id, sch_id),
    CONSTRAINT fk_add_number_patient FOREIGN KEY (patient_id) REFERENCES patient (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_add_number_schedule FOREIGN KEY (sch_id) REFERENCES doc_schedule_record (id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- Table Triggers

create trigger add_number_notify_trigger after
insert
    or
update
    on
    add_number_source_record for each row execute function notify_add_number_change();

create trigger trg_add_number_insert_message after
insert
    on
    add_number_source_record for each row execute function fn_add_number_insert_message();