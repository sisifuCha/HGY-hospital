-- PostgreSQL 触发器：当 message_record 有新消息时发送通知到医生端
-- 用于实时推送消息到医生端 SSE (/doctor/notifications)

-- 1. 创建通知函数
CREATE OR REPLACE FUNCTION notify_message_change()
RETURNS TRIGGER AS $$
DECLARE
    doc_rec RECORD;
    payload_json TEXT;
BEGIN
    -- 只处理未过期的未确认消息
    IF NEW.status != 'expired' AND NEW.read_status = 'unconfirmed' THEN
        
        -- 根据不同的 receiver_type 处理
        CASE NEW.receiver_type
            
            -- 发送给特定医生
            WHEN 'specific_doctor' THEN
                IF NEW.receiver_id IS NOT NULL THEN
                    payload_json := json_build_object('docId', NEW.receiver_id, 'messageId', NEW.id)::TEXT;
                    RAISE NOTICE '触发器: 向医生 % 发送消息通知 (消息ID: %)', NEW.receiver_id, NEW.id;
                    PERFORM pg_notify('message_channel', payload_json);
                END IF;
            
            -- 发送给科室所有医生
            WHEN 'department_doctors' THEN
                IF NEW.receiver_id IS NOT NULL THEN
                    -- 查询该科室下的所有医生
                    FOR doc_rec IN 
                        SELECT DISTINCT d."id" AS doc_id
                        FROM "doctor" d
                        WHERE d."depart_id" = NEW.receiver_id
                    LOOP
                        payload_json := json_build_object('docId', doc_rec.doc_id, 'messageId', NEW.id)::TEXT;
                        RAISE NOTICE '触发器: 向科室医生 % 发送消息通知 (消息ID: %)', doc_rec.doc_id, NEW.id;
                        PERFORM pg_notify('message_channel', payload_json);
                    END LOOP;
                END IF;
            
            -- 发送给所有医生
            WHEN 'all_doctors' THEN
                FOR doc_rec IN 
                    SELECT DISTINCT d."id" AS doc_id
                    FROM "doctor" d
                LOOP
                    payload_json := json_build_object('docId', doc_rec.doc_id, 'messageId', NEW.id)::TEXT;
                    RAISE NOTICE '触发器: 向所有医生 % 发送消息通知 (消息ID: %)', doc_rec.doc_id, NEW.id;
                    PERFORM pg_notify('message_channel', payload_json);
                END LOOP;
            
            ELSE
                -- 其他类型暂不处理 (specific_patient, all_patients, specific_group)
                RAISE NOTICE '触发器: 跳过非医生相关消息类型 %', NEW.receiver_type;
        END CASE;
        
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 2. 创建触发器（仅在 INSERT 后触发，避免 UPDATE 导致的死循环）
DROP TRIGGER IF EXISTS message_record_notify_trigger ON message_record;

CREATE TRIGGER message_record_notify_trigger
AFTER INSERT ON message_record
FOR EACH ROW
EXECUTE FUNCTION notify_message_change();

-- 使用说明：
-- 1. 在数据库执行此脚本创建触发器
-- 2. 后端通过 LISTEN 'message_channel' 监听通知
-- 3. 收到通知后调用 emitNotificationSnapshot(docId) 推送 SSE 到 /doctor/notifications
-- 4. 触发器会自动过滤：
--    - status != 'expired' (非过期消息)
--    - read_status = 'unconfirmed' (未确认消息)
--    - 根据 receiver_type 发送给不同范围的医生

-- 测试示例：
-- INSERT INTO message_record (title, content, sender_type, receiver_type, receiver_id, status, read_status)
-- VALUES ('测试消息', '这是一条测试消息', 'system', 'specific_doctor', 'DOC001', 'unsent', 'unconfirmed');
-- 
-- INSERT INTO message_record (title, content, sender_type, receiver_type, receiver_id, status, read_status)
-- VALUES ('科室通知', '科室会议通知', 'system', 'department_doctors', 'DEP001', 'unsent', 'unconfirmed');
--
-- INSERT INTO message_record (title, content, sender_type, receiver_type, status, read_status)
-- VALUES ('全员通知', '系统维护通知', 'system', 'all_doctors', 'unsent', 'unconfirmed');
