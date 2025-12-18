-- 候补规则配置表
-- 执行日期: 2025-12-18

-- 1. 候补规则表
CREATE TABLE waiting_rule (
    id SERIAL PRIMARY KEY,
    rule_name VARCHAR(100) NOT NULL UNIQUE,
    rule_value INTEGER NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE waiting_rule IS '候补规则配置表';
COMMENT ON COLUMN waiting_rule.rule_name IS '规则名称';
COMMENT ON COLUMN waiting_rule.rule_value IS '规则数值';
COMMENT ON COLUMN waiting_rule.description IS '规则描述';

-- 插入默认候补规则
INSERT INTO waiting_rule (rule_name, rule_value, description) VALUES
('MAX_WAITING_COUNT', 100, '单个排班最大候补人数'),
('MAX_PATIENT_WAITING', 5, '单个患者最大候补数量'),
('STOP_HOURS_BEFORE', 3, '就诊前几小时停止候补');

-- 2. 候补记录表（持久化存储）
DROP TABLE IF EXISTS alternate_record;
CREATE TABLE alternate_record (
    patient_id VARCHAR(20) NOT NULL,
    sch_id VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT '候补中',
    waiting_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    promoted_time TIMESTAMP,
    expired_time TIMESTAMP,
    position INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (patient_id, sch_id),
    CONSTRAINT FK_WAITING_PATIENT FOREIGN KEY (patient_id) REFERENCES patient(id) ON DELETE CASCADE,
    CONSTRAINT FK_WAITING_SCHEDULE FOREIGN KEY (sch_id) REFERENCES doc_schedule_record(id) ON DELETE CASCADE,
    CONSTRAINT CHK_WAITING_STATUS CHECK (status IN ('候补中', '已转正', '已过期', '已取消'))
);

COMMENT ON TABLE alternate_record IS '候补记录表';
COMMENT ON COLUMN alternate_record.status IS '候补状态: 候补中, 已转正, 已过期, 已取消';
COMMENT ON COLUMN alternate_record.waiting_time IS '加入候补时间';
COMMENT ON COLUMN alternate_record.promoted_time IS '转正时间';
COMMENT ON COLUMN alternate_record.expired_time IS '过期时间';
COMMENT ON COLUMN alternate_record.position IS '候补队列位置';

-- 索引
CREATE INDEX idx_waiting_patient ON alternate_record(patient_id);
CREATE INDEX idx_waiting_schedule ON alternate_record(sch_id);
CREATE INDEX idx_waiting_status ON alternate_record(status);
CREATE INDEX idx_waiting_time ON alternate_record(waiting_time);

-- 3. 退号额度表
CREATE TABLE refund_rate (
    id SERIAL PRIMARY KEY,
    hours_before NUMERIC(10, 2) NOT NULL,
    refund_rate NUMERIC(3, 2) NOT NULL,
    description VARCHAR(200),
    sort_order INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT CHK_REFUND_RATE CHECK (refund_rate >= 0 AND refund_rate <= 1),
    CONSTRAINT CHK_HOURS_BEFORE CHECK (hours_before >= 0)
);

COMMENT ON TABLE refund_rate IS '退号额度表';
COMMENT ON COLUMN refund_rate.hours_before IS '提前小时数（距离就诊时间）';
COMMENT ON COLUMN refund_rate.refund_rate IS '退款比例（0-1之间）';
COMMENT ON COLUMN refund_rate.sort_order IS '排序顺序（用于阶梯匹配）';

-- 插入默认退款规则（按时间梯度递减）
INSERT INTO refund_rate (hours_before, refund_rate, description, sort_order) VALUES
(24, 1.00, '提前1天以上全额退款', 1),
(12, 0.80, '提前0.5天退款80%', 2),
(6, 0.50, '提前6小时退款50%', 3),
(3, 0.30, '提前3小时退款30%', 4),
(1, 0.10, '提前1小时退款10%', 5),
(0, 0.00, '1小时内不予退款', 6);

-- 4. 为 alternate_record 添加唯一约束（防止重复候补）
CREATE UNIQUE INDEX idx_unique_patient_schedule_waiting 
ON alternate_record(patient_id, sch_id) 
WHERE status = '候补中';
