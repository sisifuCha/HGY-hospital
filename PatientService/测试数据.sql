-- 测试数据准备脚本
-- 用于挂号、候补、退款规则测试

-- ====================================
-- 1. 测试用排班数据（doc_schedule_record）
-- ====================================

-- 有号源的排班（用于正常挂号测试）
INSERT INTO doc_schedule_record (id, template_id, schedule_date, left_source_count, doc_id, clinic_id, status, reason)
VALUES 
('SCH001', 'TIME0001', '2025-12-20', 10, 'DOC0007', 'CLIN013', 0, NULL),
('SCH002', 'TIME0002', '2025-12-21', 8, 'DOC0143', 'CLIN0101', 0, NULL),
('SCH003', 'TIME0001', '2025-12-22', 5, 'DOC0055', 'CLIN0102', 0, NULL),
('SCH004', 'TIME0002', '2025-12-23', 3, 'DOC0007', 'CLIN013', 0, NULL)
ON CONFLICT (id) DO UPDATE SET left_source_count = EXCLUDED.left_source_count;

-- 无号源的排班（用于候补测试）
INSERT INTO doc_schedule_record (id, template_id, schedule_date, left_source_count, doc_id, clinic_id, status, reason)
VALUES 
('SCH_NO_SOURCE', 'TIME0001', '2025-12-24', 0, 'DOC0007', 'CLIN013', 0, NULL),
('SCH_NO_SOURCE_001', 'TIME0002', '2025-12-25', 0, 'DOC0143', 'CLIN0101', 0, NULL)
ON CONFLICT (id) DO UPDATE SET left_source_count = 0;

-- 候补转正测试专用排班
INSERT INTO doc_schedule_record (id, template_id, schedule_date, left_source_count, doc_id, clinic_id, status, reason)
VALUES 
('SCH_PROMOTE_001', 'TIME0001', '2025-12-26', 1, 'DOC0055', 'CLIN0102', 0, NULL),
('SCH_PROMOTE_002', 'TIME0002', '2025-12-27', 1, 'DOC0007', 'CLIN013', 0, NULL),
('SCH_PROMOTE_003', 'TIME0001', '2025-12-28', 2, 'DOC0143', 'CLIN0101', 0, NULL),
('SCH_PROMOTE_004', 'TIME0002', '2025-12-29', 1, 'DOC0055', 'CLIN0102', 0, NULL),
('SCH_PROMOTE_005', 'TIME0001', '2025-12-30', 1, 'DOC0007', 'CLIN013', 0, NULL)
ON CONFLICT (id) DO UPDATE SET left_source_count = EXCLUDED.left_source_count;

-- ====================================
-- 2. 测试用患者数据（patient）
-- ====================================

-- 普通挂号患者
INSERT INTO patient (id, birth, id_num, medical_insuranceid, reimburse_id)
VALUES 
('PAT001', '1990-01-01', '110101199001010001', 'PAT001INS', 'RT002'),
('PAT002', '1985-05-15', '110101198505150002', 'PAT002INS', 'RT002'),
('PAT003', '1992-08-20', '110101199208200003', 'PAT003INS', 'RT002'),
('PAT004', '1988-03-10', '110101198803100004', 'PAT004INS', 'RT002'),
('PAT005', '1995-11-25', '110101199511250005', 'PAT005INS', 'RT002')
ON CONFLICT (id) DO NOTHING;

-- 候补患者
INSERT INTO patient (id, birth, id_num, medical_insuranceid, reimburse_id)
VALUES 
('PAT_WAIT_001', '1991-02-14', '110101199102140011', 'WAIT001INS', 'RT002'),
('PAT_WAIT_002', '1993-07-08', '110101199307080012', 'WAIT002INS', 'RT002'),
('PAT_WAIT_003', '1989-12-30', '110101198912300013', 'WAIT003INS', 'RT002'),
('PAT_WAIT_004', '1994-04-22', '110101199404220014', 'WAIT004INS', 'RT002')
ON CONFLICT (id) DO NOTHING;

-- 候补转正测试专用患者
INSERT INTO patient (id, birth, id_num, medical_insuranceid, reimburse_id)
VALUES 
('PAT_REG_001', '1987-06-15', '110101198706150021', 'REG001INS', 'RT002'),
('PAT_REG_002', '1990-09-18', '110101199009180022', 'REG002INS', 'RT002'),
('PAT_REG_003', '1992-11-05', '110101199211050023', 'REG003INS', 'RT002'),
('PAT_REG_004', '1986-03-27', '110101198603270024', 'REG004INS', 'RT002'),
('PAT_REG_005', '1993-08-14', '110101199308140025', 'REG005INS', 'RT002'),
('PAT_WAIT_P1', '1991-05-20', '110101199105200031', 'WAITP1INS', 'RT002'),
('PAT_WAIT_P2', '1988-12-08', '110101198812080032', 'WAITP2INS', 'RT002'),
('PAT_WAIT_P3', '1995-01-30', '110101199501300033', 'WAITP3INS', 'RT002'),
('PAT_WAIT_004', '1992-07-22', '110101199207220034', 'WAIT4INS', 'RT002'),
('PAT_WAIT_005', '1989-10-11', '110101198910110035', 'WAIT5INS', 'RT002')
ON CONFLICT (id) DO NOTHING;

-- ====================================
-- 3. 验证数据
-- ====================================

-- 检查排班数据
SELECT '排班数据' as category, id, schedule_date, left_source_count, doc_id 
FROM doc_schedule_record 
WHERE id LIKE 'SCH%'
ORDER BY schedule_date;

-- 检查患者数据
SELECT '患者数据' as category, id, birth, medical_insuranceid 
FROM patient 
WHERE id IN ('PAT001', 'PAT002', 'PAT_WAIT_001', 'PAT_REG_001')
ORDER BY id;

-- 检查规则配置
SELECT '候补规则' as category, rule_name, rule_value, description 
FROM waiting_rule
UNION ALL
SELECT '退款规则', hours_before::text, (refund_rate * 100)::text || '%', description 
FROM refund_rate 
ORDER BY category DESC, rule_value;

COMMIT;
