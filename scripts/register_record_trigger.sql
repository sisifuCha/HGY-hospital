-- 触发器函数：监听 register_record 表的变动并插入 message_record
CREATE OR REPLACE FUNCTION notify_register_record_change()
RETURNS TRIGGER AS $$
DECLARE
    msg_title VARCHAR(200);
    msg_content TEXT;
    msg_type VARCHAR(50); -- 用于判断逻辑的类型变量
    receiver_id VARCHAR(20);
BEGIN
    -- 获取接收者ID (患者ID)
    IF TG_OP = 'INSERT' THEN
        receiver_id := NEW.patient_id;
    ELSE
        receiver_id := NEW.patient_id;
    END IF;

    -- 1. 处理 INSERT 操作
    IF TG_OP = 'INSERT' THEN
        msg_title := '挂号提交成功';
        msg_content := '您的挂号申请已提交，请等待确认。';
        
        INSERT INTO message_record (title, content, sender_type, receiver_type, receiver_id, status, created_time)
        VALUES (msg_title, msg_content, 'system', 'specific_patient', receiver_id, 'unsent', NOW());
    
    -- 2. 处理 UPDATE 操作
    ELSIF TG_OP = 'UPDATE' THEN
        -- 留出判断逻辑接口
        -- 这里可以根据 NEW.status 和 OLD.status 来确定 msg_type
        -- 示例逻辑：
        IF NEW.status = '已预约' AND OLD.status != '已预约' THEN
            msg_type := 'a';
        ELSIF NEW.status = '已取消' AND OLD.status != '已取消' THEN
            msg_type := 'b';
        ELSE
            msg_type := 'other';
        END IF;

        -- 根据 msg_type 插入不同消息
        IF msg_type = 'a' THEN
            msg_title := '挂号预约成功';
            msg_content := '您的挂号申请已成功预约，请准时就诊。';
            
            INSERT INTO message_record (title, content, sender_type, receiver_type, receiver_id, status, created_time)
            VALUES (msg_title, msg_content, 'system', 'specific_patient', receiver_id, 'unsent', NOW());
            
        ELSIF msg_type = 'b' THEN
            msg_title := '挂号已取消';
            msg_content := '您的挂号记录已取消。';
            
            INSERT INTO message_record (title, content, sender_type, receiver_type, receiver_id, status, created_time)
            VALUES (msg_title, msg_content, 'system', 'specific_patient', receiver_id, 'unsent', NOW());
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 创建触发器
DROP TRIGGER IF EXISTS trg_register_record_notify ON register_record;
CREATE TRIGGER trg_register_record_notify
AFTER INSERT OR UPDATE ON register_record
FOR EACH ROW
EXECUTE FUNCTION notify_register_record_change();
