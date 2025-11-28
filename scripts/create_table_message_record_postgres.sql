-- PostgreSQL 版本：创建 message_record 表并添加触发器更新 updated_time
-- 说明：原文件使用了 MySQL 风格的 `COMMENT` 内联注释，PostgreSQL 不支持该语法。

CREATE TABLE IF NOT EXISTS message_record (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    sender_type VARCHAR(20) NOT NULL,
    receiver_type VARCHAR(30) NOT NULL,
    receiver_id VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'unsent',
    read_status VARCHAR(20) NOT NULL DEFAULT 'unconfirmed',
    created_time TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    over_time TIMESTAMP WITHOUT TIME ZONE
);

-- 用 COMMENT ON 添加表和列注释（可选）
COMMENT ON TABLE message_record IS '消息记录表';
COMMENT ON COLUMN message_record.title IS '消息标题';
COMMENT ON COLUMN message_record.content IS '消息内容';
COMMENT ON COLUMN message_record.sender_type IS '发送方类型：system：系统, admin：管理员';
COMMENT ON COLUMN message_record.receiver_type IS '接收方类型：specific_patient，specific_doctor，department_doctors，all_doctors，all_patients，specific_group';
COMMENT ON COLUMN message_record.receiver_id IS '接收者ID，当接收方为特定用户时使用';
COMMENT ON COLUMN message_record.status IS '消息状态：unsent：未发送, sent：已发送, expired：已过期';
COMMENT ON COLUMN message_record.read_status IS '阅读状态：unconfirmed：未确认, confirmed：已确认';
COMMENT ON COLUMN message_record.created_time IS '创建时间';
COMMENT ON COLUMN message_record.updated_time IS '更新时间';
COMMENT ON COLUMN message_record.over_time IS '过期时间，到期后状态自动变更为已过期';

-- 索引
CREATE INDEX IF NOT EXISTS idx_message_receiver_type_id ON message_record(receiver_type, receiver_id);
CREATE INDEX IF NOT EXISTS idx_message_status ON message_record(status);
CREATE INDEX IF NOT EXISTS idx_message_created_time ON message_record(created_time);
CREATE INDEX IF NOT EXISTS idx_message_over_time ON message_record(over_time);

-- 创建触发函数（PostgreSQL 需要指定 LANGUAGE plpgsql）
CREATE OR REPLACE FUNCTION update_updated_time_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 创建触发器
DROP TRIGGER IF EXISTS update_message_updated_time ON message_record;
CREATE TRIGGER update_message_updated_time
BEFORE UPDATE ON message_record
FOR EACH ROW
EXECUTE FUNCTION update_updated_time_column();
