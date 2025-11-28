-- PostgreSQL 触发器：当 add_number_source_record 有新待处理记录时发送通知
-- 用于实时推送加号申请到医生端 SSE

-- 1. 创建通知函数
CREATE OR REPLACE FUNCTION notify_add_number_change()
RETURNS TRIGGER AS $$
DECLARE
    doc_id_val VARCHAR(20);
BEGIN
    -- 获取对应医生ID（通过关联 doc_schedule_record）
    SELECT dsr.doc_id INTO doc_id_val
    FROM doc_schedule_record dsr
    WHERE dsr.id = NEW.sch_id;
    
    -- 如果是新插入的待审核记录或状态改为待审核
    IF (TG_OP = 'INSERT' AND NEW.status = '待审核') OR 
       (TG_OP = 'UPDATE' AND NEW.status = '待审核' AND (OLD.status IS NULL OR OLD.status != '待审核')) THEN
        -- 发送通知到 add_number_channel，payload 为医生ID
        RAISE NOTICE '触发器: 向医生 % 发送加号通知', doc_id_val;
        PERFORM pg_notify('add_number_channel', doc_id_val);
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 2. 创建触发器（在 INSERT 或 UPDATE 后触发）
DROP TRIGGER IF EXISTS add_number_notify_trigger ON add_number_source_record;

CREATE TRIGGER add_number_notify_trigger
AFTER INSERT OR UPDATE ON add_number_source_record
FOR EACH ROW
EXECUTE FUNCTION notify_add_number_change();

-- 使用说明：
-- 1. 在数据库执行此脚本创建触发器
-- 2. 后端通过 LISTEN 'add_number_channel' 监听通知
-- 3. 收到通知后调用 emitAddNumberSnapshot(docId) 推送 SSE
