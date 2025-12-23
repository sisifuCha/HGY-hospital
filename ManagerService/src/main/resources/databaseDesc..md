# 医院管理系统数据库结构文档

## 1. 数据库概述

该数据库包含19个表，用于管理医院的用户、医生、患者、排班、挂号、支付等核心业务数据。数据库采用PostgreSQL设计，使用外键约束确保数据一致性，并通过索引优化查询性能。

## 2. 表结构总览

| 表名 | 主要功能 | 关联表 |
|------|---------|-------|
| user | 用户基础信息 | patient, doctor |
| title_number_source | 医生职称与号源信息 | doctor |
| sensitive_operation | 敏感操作记录 | patient |
| schedule_template | 排班时间模板 | clinic |
| reimburse_type | 报销类型 | patient |
| register_record | 挂号记录 | patient, doc_schedule_record |
| pay_record | 支付记录 | patient, doc_schedule_record |
| patient | 患者信息 | user, medical_insurance, reimburse_type |
| message_record | 消息记录 | - |
| medical_insurance | 医保信息 | patient |
| doctor | 医生信息 | user, title_number_source, clinic, department |
| doc_schedule_record | 医生排班记录 | doctor, schedule_template |
| doc_schedule_change_record | 排班变更记录 | doctor, doc_schedule_record |
| department | 科室信息 | clinic |
| clinic | 诊室信息 | department |
| cancel_record | 取消挂号记录 | patient, doc_schedule_record |
| blacklist | 黑名单记录 | sensitive_operation |
| alternate_record | 候补挂号记录 | patient, doc_schedule_record |
| add_number_source_record | 加号申请记录 | patient, doc_schedule_record |
| register_rule | 挂号规则 | - |

## 3. 各表详细结构

### 3.1 user表

**功能**：存储系统所有用户的基础信息

| 列名 | 数据类型 | 约束 | 说明 |
|------|---------|------|------|
| id | varchar(20) | PRIMARY KEY | 用户ID |
| email | varchar(100) | NULL | 邮箱 |
| pass | varchar(255) | NULL | 密码 |
| name | varchar(50) | NULL | 姓名 |
| account | varchar(50) | NULL | 账号 |
| sex | varchar(10) | NULL | 性别 |
| phone_num | varchar(20) | NULL | 手机号 |
| user_type | varchar(3) | NOT NULL | 用户类型 |

**索引**：
- idx_user_account: account
- idx_user_email: email
- idx_user_phone: phone_num

### 3.2 title_number_source表

**功能**：存储医生职称和号源配置

| 列名 | 数据类型 | 约束 | 说明 |
|------|---------|------|------|
| id | varchar(20) | PRIMARY KEY | 职称ID |
| name | varchar(10) | NOT NULL | 职称名称 |
| number_source_count | int2 | NULL, CHECK(number_source_count >= 0) | 号源数量 |
| ori_cost | numeric(10, 2) | NULL | 原始费用 |

### 3.3 sensitive_operation表

**功能**：记录敏感操作

| 列名 | 数据类型 | 约束 | 说明 |
|------|---------|------|------|
| id | varchar(20) | PRIMARY KEY | 操作记录ID |
| patient_id | varchar(20) | NULL, FOREIGN KEY REFERENCES patient(id) | 患者ID |
| sensitive_op_type | varchar(50) | NULL | 敏感操作类型 |
| op_time | timestamp | NULL | 操作时间 |
| remark | text | NULL | 备注 |

**索引**：
- idx_sensitive_patient: patient_id
- idx_sensitive_time: op_time
- idx_sensitive_type: sensitive_op_type

### 3.4 schedule_template表

**功能**：定义排班时间模板

| 列名 | 数据类型 | 约束 | 说明 |
|------|---------|------|------|
| id | varchar(20) | PRIMARY KEY | 模板ID |
| start_time | time | NULL | 开始时间 |
| end_time | time | NULL | 结束时间 |
| clin_id | varchar(20) | NULL, FOREIGN KEY REFERENCES clinic(id) | 诊室ID |
| time_period_name | varchar(20) | NULL | 时间段名称 |

**索引**：
- idx_schedule_template_clinic: clin_id

### 3.5 reimburse_type表

**功能**：定义报销类型

| 列名 | 数据类型 | 约束 | 说明 |
|------|---------|------|------|
| id | varchar(20) | PRIMARY KEY | 报销类型ID |
| type | varchar(50) | NULL | 类型名称 |
| percent | numeric(5, 2) | NULL, CHECK(percent >= 0 AND percent <= 100) | 报销比例 |

### 3.6 register_record表

**功能**：记录挂号信息

| 列名 | 数据类型 | 约束 | 说明 |
|------|---------|------|------|
| patient_id | varchar(20) | PRIMARY KEY, FOREIGN KEY REFERENCES patient(id) | 患者ID |
| sch_id | varchar(20) | PRIMARY KEY, FOREIGN KEY REFERENCES doc_schedule_record(id) | 排班ID |
| register_time | timestamp | NULL | 挂号时间 |
| status | varchar(20) | NULL | 挂号状态：待支付/已挂号/就诊中/已就诊/已取消'|

**索引**：
- idx_register_patient: patient_id
- idx_register_schedule: sch_id
- idx_register_status: status
- idx_register_time: register_time

### 3.7 pay_record表

**功能**：记录支付信息

| 列名 | 数据类型 | 约束 | 说明 |
|------|---------|------|------|
| id | varchar(100) | PRIMARY KEY | 支付记录ID |
| pay_time | timestamp | NULL | 支付时间 |
| pay_status | varchar(20) | NULL | 支付状态 |
| ori_amount | numeric(10, 2) | NULL, CHECK(ori_amount >= 0) | 原始金额 |
| ask_pay_amount | numeric(10, 2) | NULL, CHECK(ask_pay_amount >= 0) | 实际支付金额 |
| patient_id | varchar(20) | NULL, FOREIGN KEY REFERENCES patient(id) | 患者ID |
| sch_id | varchar(20) | NULL, FOREIGN KEY REFERENCES doc_schedule_record(id) | 排班ID |

**索引**：
- idx_pay_patient: patient_id
- idx_pay_status: pay_status
- idx_pay_time: pay_time

### 3.8 patient表

**功能**：存储患者详细信息

| 列名 | 数据类型 | 约束 | 说明 |
|------|---------|------|------|
| id | varchar(20) | PRIMARY KEY, FOREIGN KEY REFERENCES user(id) | 患者ID（关联用户表） |
| birth | date | NULL, CHECK(birth <= CURRENT_DATE) | 出生日期 |
| id_num | varchar(18) | NULL | 身份证号 |
| medical_insuranceid | varchar(20) | NULL, FOREIGN KEY REFERENCES medical_insurance(id) | 医保ID |
| reimburse_id | varchar(20) | NULL, FOREIGN KEY REFERENCES reimburse_type(id) | 报销类型ID |

**索引**：
- idx_patient_id_num: id_num
- idx_patient_medical_insurance: medical_insuranceid

### 3.9 message_record表

**功能**：系统消息记录

| 列名 | 数据类型 | 约束 | 说明 |
|------|---------|------|------|
| id | bigserial | PRIMARY KEY | 消息ID |
| title | varchar(200) | NOT NULL | 消息标题 |
| content | text | NOT NULL | 消息内容 |
| sender_type | varchar(20) | NOT NULL | 发送者类型 |
| receiver_type | varchar(30) | NOT NULL | 接收者类型 |
| receiver_id | varchar(20) | NULL | 接收者ID |
| status | varchar(20) | NOT NULL, DEFAULT 'unsent' | 消息状态 |
| read_status | varchar(20) | NOT NULL, DEFAULT 'unconfirmed' | 阅读状态 |
| created_time | timestamp | NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_time | timestamp | NULL, DEFAULT CURRENT_TIMESTAMP | 更新时间 |
| over_time | timestamp | NULL | 过期时间 |

**索引**：
- idx_message_created_time: created_time
- idx_message_over_time: over_time
- idx_message_receiver_type_id: receiver_type, receiver_id
- idx_message_status: status

**触发器**：
- message_record_notify_trigger: 插入后执行notify_message_change()
- update_message_updated_time: 更新前执行update_updated_time_column()

### 3.10 medical_insurance表

**功能**：存储医保信息

| 列名 | 数据类型 | 约束 | 说明 |
|------|---------|------|------|
| id | varchar(20) | PRIMARY KEY | 医保ID |
| overage | numeric(10, 2) | NULL, CHECK(overage >= 0) | 余额 |

### 3.11 doctor表

**功能**：存储医生详细信息

| 列名 | 数据类型 | 约束 | 说明 |
|------|---------|------|------|
| id | varchar(20) | PRIMARY KEY, FOREIGN KEY REFERENCES user(id) | 医生ID（关联用户表） |
| doc_title_id | varchar(20) | NULL, FOREIGN KEY REFERENCES title_number_source(id) | 职称ID |
| status | varchar(20) | NULL | 医生状态 |
| clinic_id | varchar(20) | NULL, FOREIGN KEY REFERENCES clinic(id) | 诊室ID |
| details | varchar(255) | NULL | 医生详情 |
| specialty | varchar(255) | NULL | 专业特长 |
| depart_id | varchar(20) | NULL, FOREIGN KEY REFERENCES department(id) | 科室ID |

**索引**：
- idx_doctor_clinic: clinic_id
- idx_doctor_status: status
- idx_doctor_title: doc_title_id

### 3.12 doc_schedule_record表

**功能**：记录医生排班信息

| 列名 | 数据类型 | 约束 | 说明 |
|------|---------|------|------|
| id | varchar(20) | PRIMARY KEY | 排班记录ID |
| template_id | varchar(20) | NULL, FOREIGN KEY REFERENCES schedule_template(id) | 时间模板ID |
| schedule_date | date | NULL | 排班日期 |
| left_source_count | int4 | NULL, CHECK(left_source_count >= 0) | 剩余号源 |
| doc_id | varchar(20) | NULL, FOREIGN KEY REFERENCES doctor(id) | 医生ID |
| clinic_id | varchar(20) | NULL | 诊室ID |
| status | int4 | NULL, DEFAULT 0 | 状态，0代表正在启用，1代表已停用 |
| reason | varchar(50) | NULL | 备注 |

**索引**：
- idx_schedule_record_date: schedule_date
- idx_schedule_record_doctor: doc_id

### 3.13 doc_schedule_change_record表

**功能**：记录排班变更申请
doc_schedule_change_record中type=0为调班（调到空位置），1为请假（取消该班次）
注意！！！target_sch_id 和 leave_time_length这两个属性已经被弃用了

| 列名 | 数据类型 | 约束 | 说明 |
|------|---------|------|------|
| doc_id | varchar(20) | NOT NULL, FOREIGN KEY REFERENCES doctor(id) | 医生ID |
| ori_sch_id | varchar(20) | PRIMARY KEY, FOREIGN KEY REFERENCES doc_schedule_record(id) | 原排班ID |
| reason_text | text | NULL | 变更原因 |
| status | varchar(20) | NULL | 变更状态 |
| target_sch_id | varchar(20) | NULL | 目标排班ID |
| target_date | date | NULL | 目标日期 |
| template_id | varchar(20) | NULL | 时间模板ID |
| type | int4 | NULL | 变更类型 |
| leave_time_length | int4 | NULL | 请假时长 |

### 3.14 department表

**功能**：记录医院科室信息

| 列名 | 数据类型 | 约束 | 说明 |
|------|---------|------|------|
| id | varchar(20) | PRIMARY KEY | 科室ID |
| name | varchar(100) | NULL | 科室名称 |
| father_id | varchar(20) | NULL, FOREIGN KEY REFERENCES department(id) | 父科室ID |

**索引**：
- idx_department_father: father_id
- idx_department_name: name

### 3.15 clinic表

**功能**：记录诊室信息

| 列名 | 数据类型 | 约束 | 说明 |
|------|---------|------|------|
| id | varchar(20) | PRIMARY KEY | 诊室ID |
| clinic_number | varchar(20) | NULL | 诊室编号 |
| location | varchar(100) | NULL | 诊室位置 |
| dep_id | varchar(20) | NULL, FOREIGN KEY REFERENCES department(id) | 科室ID |

**索引**：
- idx_clinic_department: dep_id

### 3.16 cancel_record表

**功能**：记录取消挂号信息

| 列名 | 数据类型 | 约束 | 说明 |
|------|---------|------|------|
| patient_id | varchar(20) | PRIMARY KEY, FOREIGN KEY REFERENCES patient(id) | 患者ID |
| sch_id | varchar(20) | PRIMARY KEY, FOREIGN KEY REFERENCES doc_schedule_record(id) | 排班ID |
| cancel_time | timestamp | NULL | 取消时间 |
| reason_text | text | NULL | 取消原因 |
| reason_pic | varchar(255) | NULL | 取消原因图片 |

### 3.17 blacklist表

**功能**：记录黑名单信息

| 列名 | 数据类型 | 约束 | 说明 |
|------|---------|------|------|
| id | varchar(20) | PRIMARY KEY | 黑名单ID |
| sen_id1 | varchar(20) | NULL, FOREIGN KEY REFERENCES sensitive_operation(id) | 敏感操作记录1 |
| sen_id2 | varchar(20) | NULL, FOREIGN KEY REFERENCES sensitive_operation(id) | 敏感操作记录2 |
| sen_id3 | varchar(20) | NULL, FOREIGN KEY REFERENCES sensitive_operation(id) | 敏感操作记录3 |
| count | int2 | NULL, CHECK(count >= 0) | 次数 |

### 3.18 alternate_record表

**功能**：记录候补挂号信息

| 列名 | 数据类型 | 约束 | 说明 |
|------|---------|------|------|
| patient_id | varchar(20) | PRIMARY KEY, FOREIGN KEY REFERENCES patient(id) | 患者ID |
| sch_id | varchar(20) | PRIMARY KEY, FOREIGN KEY REFERENCES doc_schedule_record(id) | 排班ID |
| register_time | timestamp | NULL | 候补时间 |
| status | varchar(20) | NULL | 候补状态 |

### 3.19 add_number_source_record表

**功能**：记录加号申请信息

| 列名 | 数据类型 | 约束 | 说明 |
|------|---------|------|------|
| patient_id | varchar(20) | PRIMARY KEY, FOREIGN KEY REFERENCES patient(id) | 患者ID |
| sch_id | varchar(20) | PRIMARY KEY, FOREIGN KEY REFERENCES doc_schedule_record(id) | 排班ID |
| apply_time | timestamp | NULL | 申请时间 |
| status | varchar(20) | NULL, DEFAULT '待审核' | 申请状态 |
| reason_text | text | NULL | 申请原因 |
| reason_pic | varchar(255) | NULL | 申请原因图片 |

**触发器**：
- add_number_notify_trigger: 插入或更新后执行notify_add_number_change()
- trg_add_number_insert_message: 插入后执行fn_add_number_insert_message()

### 3.20 waiting_rule表

**功能**：记录候补规则信息

| 列名 | 数据类型 | 约束 | 说明 |
|------|---------|------|------|
| id | integer | PRIMARY KEY | 规则ID |
| rule_name | varchar(100) | NULL | 规则名称 |
| rule_value | integer | NULL | 规则值 |
| description | varchar(500) | NULL | 规则描述 |
| created_at | timestamp | NULL | 创建时间 |
| updated_at | timestamp | NULL | 更新时间 |


## 4. 表间依赖关系

### 4.1 核心依赖链