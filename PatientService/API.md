# 红果园校医院患者端 API 文档

> **版本**：v2.0.0  
> **更新时间**：2025-12-16  
> **适用范围**：PatientService 微服务，面向患者移动端/小程序。本文档描述所有甘特图范围内的患者端功能（旧功能 + 新功能）并统一字段、状态与错误码。

---
## 0. 通用约定
- 所有应答包裹在统一结构：
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {}
  }
  ```
- HTTP 状态码与业务 `code` 对齐：400 参数错误、401 未认证、403 无权限、404 未找到、409 业务冲突、423 资源锁定、500 服务异常。
- 除 `/user/register` `/user/login` 外，所有接口必须在 Header 中携带 `Authorization: Bearer <accessToken>`，系统会校验 JWT 是否可靠、是否过期，且 body/query 中的 `patientId` 必须与 token 中的 `sub` 一致。
- 登录成功会返回 `accessToken`（默认 30 分钟有效）与 `refreshToken`（默认 7 天有效），可通过刷新接口换取新令牌；token 负载包含 `sub`(患者 ID)、`name`、`verified`(实名状态)、`scope`。
- 时间字段使用 ISO-8601（`yyyy-MM-dd'T'HH:mm:ssXXX`），日期使用 `yyyy-MM-dd`。
- 挂号状态枚举：
  - `预约中`（待支付/待确认）
  - `已预约`（完成支付/确认）
  - `已取消`
  - `已过期`
  - `已就诊`
- 候补状态枚举：`排队中` / `已转正` / `已取消` / `已过期`。
- 评价等级：1~5 星；可附文本标签数组。

---
## 1. 用户与身份认证
### 1.1 用户注册
- **POST** `/user/register`
- **Body**：
  ```json
  {
    "userAccount": "13312345678",
    "userPassword": "123456",
    "userName": "张三",
    "userGender": "男",
    "userEmail": "xxx@example.com",
    "userPhone": "13312345678",
    "birthday": "1990-07-01",
    "identificationId": "110101199007010011"
  }
  ```
- **Response**：`"注册成功"`
- **错误**：409（账号/身份证已存在）、400（身份证格式错误）。

### 1.2 用户登录
- **POST** `/user/login`
- **Body**：`{ "account": "13312345678", "password": "123456" }`
- **Response**：
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "refreshToken": "eyJhbGciOiJIUzI1NiIsInR...",
      "expiresIn": 1800,
      "tokenType": "Bearer",
      "patientId": "PAT0001",
      "verified": true
    }
  }
  ```
- **错误**：404 用户不存在；401 密码错误。

### 1.3 获取患者 ID
- **GET** `/user/patient-id?account=13312345678`
- **Response**：`"PAT0001"`

### 1.4 实名身份认证（新）
> 用于完成二次实名、证件OCR、比对黑名单等。

#### 1.4.1 提交认证
- **POST** `/api/identity/verification`
- **Body**：
  ```json
  {
    "patientId": "PAT0001",
    "name": "张三",
    "idNumber": "110101199007010011",
    "idFrontImage": "base64-...",
    "idBackImage": "base64-...",
    "faceSnapshot": "base64-..."
  }
  ```
- **Response**：
  ```json
  {
    "verificationId": "VER-20241216-0001",
    "status": "pending",
    "submitTime": "2025-12-16T09:00:00+08:00"
  }
  ```
- **错误**：400 参数缺失；409 已通过认证；423 正在审核。

#### 1.4.2 查询认证结果
- **GET** `/api/identity/verification/{patientId}`
- **Response**：
  ```json
  {
    "patientId": "PAT0001",
    "status": "approved",
    "approvedBy": "system",
    "approvedTime": "2025-12-16T10:12:00+08:00",
    "reason": null
  }
  ```
- **错误**：404 未提交认证；403 无权限（非本人）。

### 1.5 刷新令牌（新）
- **POST** `/api/auth/token/refresh`
- **Header**：`Authorization: Bearer <refreshToken>`
- **Response**：与登录成功结构一致，返回新的 `accessToken` / `expiresIn`。
- **错误**：401 refreshToken 失效；409 会话已注销。

### 1.6 查询当前会话信息（新）
- **GET** `/api/auth/session`
- **Response**：
  ```json
  {
    "patientId": "PAT0001",
    "userName": "张三",
    "verified": true,
    "scopes": ["patient"],
    "issuedAt": "2025-12-16T09:08:00+08:00",
    "expiresAt": "2025-12-16T09:38:00+08:00"
  }
  ```
- **用途**：前端启动时校验 token 状态、同步实名标记。`verified = true` 时才允许提交评价/导诊/候补。

---
## 2. 科室 / 医生 / 排班
### 2.1 获取科室树
- **GET** `/api/departments`
- **Response**：
  ```json
  [
    {
      "id": "DEP001",
      "name": "内科",
      "subDepartments": [ { "id": "DEP005", "name": "心内科门诊" } ]
    }
  ]
  ```

### 2.2 按科室+日期获取排班
- **GET** `/api/registration/doctors?departmentId=DEP005&date=2025-12-20`
- **Response**：每位医生包含 `schedules`，字段同旧版。

### 2.3 医生详情
- **GET** `/api/doctors/{doctorId}`

---
## 3. 挂号管理
### 3.1 创建挂号
- **POST** `/api/registrations`
- **Body**：`{ "patientId": "PAT0001", "scheduleRecordId": "SCH7890", "confirm": true }`
- **Response**：
  ```json
  {
    "registrationId": "REG000123",
    "patientId": "PAT0001",
    "scheduleRecordId": "SCH7890",
    "registerTime": "2025-12-20T09:30:12+08:00",
    "status": "已预约",
    "paymentStatus": "待支付"
  }
  ```
- **错误**：409 号源已满；409 已存在有效挂号；404 排班不存在；423 排班锁定。

### 3.2 分页查询挂号
- **GET** `/api/registrations`
- **Query**：`patientId`（必填），`date` / `fromDate` / `toDate` / `status`（逗号分隔），`page`，`pageSize`
- **Response**：
  ```json
  {
    "page": 1,
    "pageSize": 20,
    "total": 54,
    "items": [
      {
        "registrationId": "REG000123",
        "patientId": "PAT0001",
        "scheduleRecordId": "SCH7890",
        "doctorId": "DOC001",
        "doctorName": "张三",
        "departmentId": "DEP005",
        "departmentName": "心内科门诊",
        "clinicId": "CLIN001",
        "clinicName": "心内科诊室1",
        "scheduleDate": "2025-12-20",
        "timePeriodName": "上午",
        "status": "已预约",
        "paymentStatus": "已支付",
        "canCancel": true
      }
    ]
  }
  ```

### 3.3 复合键查询
- **GET** `/api/registrations/by-key?patientId=PAT0001&scheduleRecordId=SCH7890`
- 返回字段等同单条详情。

### 3.4 按注册 ID 查询
- **GET** `/api/registrations/{registrationId}`
- 需校验患者身份。

### 3.5 取消挂号
- **DELETE** `/api/registrations?patientId=PAT0001&scheduleRecordId=SCH7890`
- **Response**：`{ "registrationId": "REG000123", "status": "已取消" }`
- 同时触发候补转正或回补号源。

### 3.6 候补挂号
#### 3.6.1 创建候补
- **POST** `/api/registrations/waiting`
- **Body**：`{ "patientId": "PAT0001", "scheduleRecordId": "SCH7890" }`
- **Response**：`{ "waitingId": "WAIT-001", "position": 3, "status": "排队中" }`

#### 3.6.2 按排班查看候补
- **GET** `/api/registrations/waiting?scheduleRecordId=SCH7890`

#### 3.6.3 按患者查看候补
- **GET** `/api/registrations/waiting/patient?patientId=PAT0001&date=2025-12-20`

#### 3.6.4 取消候补
- **DELETE** `/api/registrations/waiting?patientId=PAT0001&waitingId=WAIT-001`

#### 3.6.5 候补确认
- **POST** `/api/registrations/waiting/confirm`
- **Body**：`{ "waitingId": "WAIT-001" }`
- 场景：系统推送号源后，患者确认转正。

---
## 4. 就诊评价（新）
### 4.1 提交评价
- **POST** `/api/registrations/{registrationId}/feedback`
- **Body**：
  ```json
  {
    "patientId": "PAT0001",
    "score": 5,
    "tags": ["医生专业", "环境舒适"],
    "comment": "医生讲解很耐心"
  }
  ```
- **规则**：仅 `status = 已就诊` 可评价；同一注册仅一次；可更新允许 `PUT`。

### 4.2 查看评价
- **GET** `/api/registrations/{registrationId}/feedback`
- **Response**：
  ```json
  {
    "registrationId": "REG000123",
    "score": 4,
    "tags": ["沟通清晰"],
    "comment": "总体满意",
    "createdTime": "2025-12-21T12:00:00+08:00"
  }
  ```

---
## 5. 消息提醒（增强）
### 5.1 拉取未发送消息
- **POST** `/api/patients/{patientId}/messages`
- **Response**：
  ```json
  {
    "status": true,
    "messages": [
      {
        "messageId": "MSG0001",
        "title": "预约成功",
        "content": "请于 9:00 前到达诊室",
        "category": "REGISTRATION",
        "timestamp": "2025-12-16T10:00:00+08:00"
      }
    ]
  }
  ```
- 调用后消息状态置为 `sent`。

### 5.2 设置消息偏好（新）
- **PUT** `/api/patients/{patientId}/notification-settings`
- **Body**：
  ```json
  {
    "channels": ["APP", "SMS"],
    "quietPeriod": { "start": "22:00", "end": "07:00" },
    "remindBeforeMinutes": 30
  }
  ```
- **Response**：`"设置已更新"`

### 5.3 获取消息偏好
- **GET** `/api/patients/{patientId}/notification-settings`

---
## 6. 智能导诊（新）
### 6.1 症状问诊建议（更新逻辑）
- **POST** `/api/triage/suggestions`
- **Body**：
  ```json
  {
    "patientId": "PAT0001",
    "symptoms": ["胸闷", "心悸"],
    "durationDays": 3,
    "temperature": 37.5,
    "allergies": ["青霉素"],
    "history": ["高血压"],
    "attachments": ["base64-自测心率图"],
    "mode": "hybrid"
  }
  ```
- **Response**：
  ```json
  {
    "recommendations": [
      {
        "departmentId": "DEP005",
        "departmentName": "心内科门诊",
        "confidence": 0.87,
        "reason": "症状特征与高风险心血管疾病匹配"
      }
    ],
    "suggestedActions": [
      "如症状加重请立即就诊",
      "前往心内科完成线下检查"
    ]
  }
  ```
- **规则说明**：
  - `mode` 参数支持：
    - `rule`：仅基于规则引擎。
    - `ml`：仅基于机器学习模型。
    - `hybrid`（默认）：结合规则与模型。
  - 返回结果包含推荐科室及建议行动。

### 6.2 查询历史问诊记录
- **GET** `/api/triage/history?patientId=PAT0001`
- **Response**：
  ```json
  [
    {
      "triageId": "TRIAGE001",
      "createdAt": "2025-12-15T10:00:00+08:00",
      "symptoms": ["胸闷", "心悸"],
      "recommendations": [
        {
          "departmentId": "DEP005",
          "departmentName": "心内科门诊",
          "confidence": 0.87
        }
      ]
    }
  ]
  ```

---
## 7. 地理导航（新）
### 7.1 获取院区与诊室坐标
- **GET** `/api/navigation/locations`
- **Response**：
  ```json
  {
    "campuses": [
      {
        "campusId": "CAMP01",
        "name": "主院区",
        "latitude": 31.2304,
        "longitude": 121.4737,
        "floors": [
          {
            "floor": "1F",
            "clinics": [
              {
                "clinicId": "CLIN001",
                "name": "心内科诊室1",
                "latitude": 31.2305,
                "longitude": 121.4738
              }
            ]
          }
        ]
      }
    ]
  }
  ```

### 7.2 生成院区导航路线
- **POST** `/api/navigation/routes`
- **Body**：
  ```json
  {
    "patientId": "PAT0001",
    "origin": { "lat": 31.229, "lng": 121.47 },
    "destinationClinicId": "CLIN001",
    "prefer": "indoor",
    "needAccessibility": true
  }
  ```
- **Response**：
  ```json
  {
    "campusId": "CAMP01",
    "distanceMeters": 320,
    "etaMinutes": 6,
    "steps": [
      { "type": "outdoor", "instruction": "从南门进入" },
      { "type": "indoor", "instruction": "乘电梯至 1F" }
    ]
  }
  ```
- **错误**：404 诊室未配置坐标；400 坐标缺失。

---
## 8. 统一错误响应示例
```json
{
  "code": 409,
  "message": "号源已满",
  "data": null
}
```

---
## 9. 数据模型摘要
| 模块 | 关键字段 | 说明 |
| ---- | -------- | ---- |
| register_record | id (registrationId), patient_id, sch_id, status, register_time, payment_status | 挂号主表 |
| waiting_queue (Redis + DB) | waiting_id, patient_id, sch_id, status | 候补队列 |
| patient_profile | patient_id, address, medical_history, allergies | 档案信息 |
| identity_verification (新) | verification_id, patient_id, status, reason, attachments | 实名认证记录 |
| registration_feedback (新) | feedback_id, registration_id, score, tags, comment | 就诊评价 |
| notification_preference (新) | patient_id, channels, quiet_period, remind_before | 消息偏好 |
| triage_record (新) | triage_id, patient_id, symptoms, recommendation | 智能导诊记录 |
| clinic_location (扩展) | clinic_id, campus_id, geo_point, floor | 地理导航 |

> **说明**：若数据库尚未建表，请结合 `database/schema_current.sql` 及 `scripts/` 目录补充建表脚本，字段需与本表保持一致。
