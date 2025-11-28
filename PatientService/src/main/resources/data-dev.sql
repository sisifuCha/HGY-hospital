-- data-dev.sql: seed a schedule record with left_source_count = 0 so waiting can be tested

MERGE INTO doc_schedule_record (id, doctor_id, schedule_date, left_source_count) KEY(id) VALUES ('SCH_TEST_1', 'DOC_TEST_1', CURRENT_DATE, 0);

-- ensure there is no existing waiting
DELETE FROM waiting_queue;

-- insert a test patient
MERGE INTO patient (id, name) KEY(id) VALUES ('17', '测试患者17');

-- ensure register_record empty for tests
DELETE FROM register_record;

-- ensure patient_extra_apply table empty
DELETE FROM patient_extra_apply;
