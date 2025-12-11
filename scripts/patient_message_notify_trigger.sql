-- PostgreSQL 触发器：当 message_record 有新消息（针对患者）时发送通知到后端
-- 替代原有的定时轮询方案

CREATE OR REPLACE FUNCTION notify_patient_message_change()
RETURNS TRIGGER AS $$
DECLARE
    payload_json TEXT;
BEGIN
    -- 只处理针对特定患者的未发送消息
    IF NEW.receiver_type = 'specific_patient' AND NEW.status = 'unsent' THEN
        IF NEW.receiver_id IS NOT NULL THEN
            -- 构建 JSON payload: {"patientId": "PAT001", "messageId": 123}
            payload_json := json_build_object('patientId', NEW.receiver_id, 'messageId', NEW.id)::TEXT;
            
            -- 发送通知到 'patient_message_channel'
            PERFORM pg_notify('patient_message_channel', payload_json);
        END IF;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 创建触发器
DROP TRIGGER IF EXISTS trg_patient_message_notify ON message_record;
CREATE TRIGGER trg_patient_message_notify
AFTER INSERT ON message_record
FOR EACH ROW
EXECUTE FUNCTION notify_patient_message_change();
