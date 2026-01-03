-- 触发器：当 add_number_source_record 插入记录时，自动向 message_record 插入通知消息
-- 创建日期: 2025-12-04

-- 删除已存在的触发器和函数
DROP TRIGGER IF EXISTS trg_add_number_insert_message ON add_number_source_record;
DROP FUNCTION IF EXISTS fn_add_number_insert_message();

-- 创建触发器函数
CREATE OR REPLACE FUNCTION fn_add_number_insert_message()
RETURNS TRIGGER AS $$
DECLARE
    v_patient_name VARCHAR(100);
    v_doc_id VARCHAR(20);
    v_schedule_date DATE;
    v_template_id VARCHAR(20);
    v_time_period VARCHAR(10);
    v_title VARCHAR(255);
    v_content TEXT;
BEGIN
    -- 1. 通过 patient_id 从 user 表获取 name (patient_id 对应 user 表的 id)
    SELECT name INTO v_patient_name
    FROM "user"
    WHERE id = NEW.patient_id;
    
    -- 如果找不到患者，使用默认值
    IF v_patient_name IS NULL THEN
        v_patient_name := '未知患者';
    END IF;
    
    -- 2. 通过 sch_id 从 doc_schedule_record 表获取 doc_id, date, template_id
    SELECT doc_id, schedule_date, template_id 
    INTO v_doc_id, v_schedule_date, v_template_id
    FROM doc_schedule_record
    WHERE id = NEW.sch_id;
    
    -- 如果找不到排班记录，跳过插入
    IF v_doc_id IS NULL THEN
        RAISE NOTICE '未找到排班记录 sch_id: %, 跳过消息插入', NEW.sch_id;
        RETURN NEW;
    END IF;
    
    -- 3. 根据 template_id 生成时段名称
    v_time_period := CASE v_template_id
        WHEN 'TIME0001' THEN '上午'
        WHEN 'TIME0002' THEN '下午'
        ELSE '未知时段'
    END;
    
    -- 4. 构建 title 和 content
    v_title := '加号申请：' || v_patient_name;
    v_content := '患者' || v_patient_name || '申请在班次' || 
                 TO_CHAR(v_schedule_date, 'YYYY-MM-DD') || ' ' || v_time_period || 
                 '加号，详情见加号栏。';
    
    -- 5. 向 message_record 表插入记录
    INSERT INTO message_record (
        title,
        content,
        sender_type,
        receiver_type,
        receiver_id,
        status,
        created_time
    ) VALUES (
        v_title,
        v_content,
        'system',
        'specific_doctor',
        v_doc_id,
        'unsent',
        NOW()
    );
    
    RAISE NOTICE '已为加号申请创建消息通知: 医生=%, 患者=%', v_doc_id, v_patient_name;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 创建触发器（仅在 INSERT 时触发）
CREATE TRIGGER trg_add_number_insert_message
    AFTER INSERT ON add_number_source_record
    FOR EACH ROW
    EXECUTE FUNCTION fn_add_number_insert_message();

-- 添加注释
COMMENT ON FUNCTION fn_add_number_insert_message() IS '当加号申请插入时，自动创建消息通知给对应医生';
COMMENT ON TRIGGER trg_add_number_insert_message ON add_number_source_record IS '加号申请插入触发器 - 自动创建消息通知';

-- 测试查询（可选，用于验证关联关系）
-- SELECT 
--     a.id as add_number_id,
--     p.name as patient_name,
--     d.doc_id,
--     d.date as schedule_date,
--     d.template_id,
--     CASE d.template_id 
--         WHEN 'TIME0001' THEN '上午'
--         WHEN 'TIME0002' THEN '下午'
--         ELSE '未知时段'
--     END as time_period
-- FROM add_number_source_record a
-- JOIN patient p ON a.patient_id = p.patient_id
-- JOIN doc_schedule_record d ON a.sch_id = d.sch_id
-- LIMIT 5;
