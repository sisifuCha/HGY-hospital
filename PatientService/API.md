# 接口文档（精简版）

本文件仅包含：
- 已经实现的接口（3个）
- 待实现的最小核心“挂号”接口（4个）

## 1. 已实现接口

### 1.1 获取科室列表
- URL: `/api/departments`
- Method: `GET`
- Description: 获取所有一级科室及其下的二级科室列表。
- Success Response (200):
  ```json
  [
    {
      "id": "DEP001",
      "name": "内科",
      "subDepartments": [
        { "id": "DEP005", "name": "心内科门诊" },
        { "id": "DEP006", "name": "肾内科门诊" },
        { "id": "DEP007", "name": "血液科门诊" },
        { "id": "DEP008", "name": "感染内科门诊" },
        { "id": "DEP009", "name": "肝炎门诊" }
      ]
    }
  ]
  ```

### 1.2 根据科室和日期获取医生排班
- URL: `/api/registration/doctors`
- Method: `GET`
- Params:
  - `departmentId` (number, required)
  - `date` (string, required, YYYY-MM-DD)
- Success Response (200):
  ```json
  [
    {
      "doctorId": "doc-001",
      "doctorName": "张三",
      "doctorTitle": "主任医师",
      "specialty": "高血压、冠心病等心血管疾病",
      "schedules": [
        {
          "scheduleId": "101",
          "timePeriodName": "上午",
          "startTime": "08:00",
          "endTime": "12:00",
          "registrationFee": 50,
          "leftSourceCount": 5
        }
      ]
    }
  ]
  ```

### 1.3 获取医生详情
- URL: `/api/doctors/{doctorId}`
- Method: `GET`
- Path Params:
  - `doctorId` (string, required)
- Success Response (200):
  ```json
  {
    "id": "doc-001",
    "name": "张三",
    "docTitleId": "T001",
    "title": "主任医师",
    "specialty": "心血管相关疾病",
    "details": "从业20年，经验丰富",
    "departId": "101",
    "clinicId": "CL001",
    "status": "ACTIVE"
  }
  ```

---
## 2. 待实现的最小核心“挂号”接口
说明：使用 patientId + scheduleRecordId 作为复合键唯一定位一条挂号记录。状态统一使用中文枚举：预约中 / 已预约 / 已取消 / 已过期 / 已就诊（预留）。

### 2.1 创建挂号
- URL: `/api/registrations`
- Method: `POST`
- Request Body:
  ```json
  {
    "patientId": "PAT123456",
    "scheduleRecordId": "SCH7890",
    "confirm": true
  }
  ```
- 字段说明：
  - `confirm`: true → 已预约；false → 预约中。
- Success Response (201):
  ```json
  {
    "patientId": "PAT123456",
    "scheduleRecordId": "SCH7890",
    "registerTime": "2025-11-15T09:30:12",
    "status": "已预约"
  }
  ```

### 2.2 查询挂号列表
- URL: `/api/registrations`
- Method: `GET`
- Query Params:
  - `patientId` (string, required)
  - `date` (string, optional, YYYY-MM-DD)
  - `fromDate` (string, optional, YYYY-MM-DD)
  - `toDate` (string, optional, YYYY-MM-DD)
  - `status` (string, optional，多状态逗号分隔，如 `已预约,预约中`)
  - `page` (number, optional, default 1)
  - `pageSize` (number, optional, default 20)
- Success Response (200):
  ```json
  {
    "page": 1,
    "pageSize": 20,
    "total": 54,
    "items": [
      {
        "patientId": "PAT123456",
        "scheduleRecordId": "SCH7890",
        "registerTime": "2025-11-15T09:30:12",
        "status": "已预约",
        "doctorId": "DOC0023",
        "departmentId": "DEP005",
        "scheduleDate": "2025-11-15",
        "timePeriodName": "上午"
      }
    ]
  }
  ```

### 2.3 查询单条挂号（按复合键）
- URL: `/api/registrations/by-key`
- Method: `GET`
- Query Params:
  - `patientId` (string, required)
  - `scheduleRecordId` (string, required)
- Success Response (200):
  ```json
  {
    "patientId": "PAT123456",
    "scheduleRecordId": "SCH7890",
    "registerTime": "2025-11-15T09:30:12",
    "status": "已预约",
    "doctorId": "DOC0023",
    "departmentId": "DEP005",
    "scheduleDate": "2025-11-15",
    "timePeriodName": "上午"
  }
  ```

### 2.4 取消挂号（按复合键）
- URL: `/api/registrations`
- Method: `DELETE`
- Query Params:
  - `patientId` (string, required)
  - `scheduleRecordId` (string, required)
- Success Response (200):
  ```json
  {
    "patientId": "PAT123456",
    "scheduleRecordId": "SCH7890",
    "status": "已取消"
  }
  ```

---
## 3. 业务与校验要点
- 身份校验：patientId 归属、黑名单、当日限额。
- 排班校验：scheduleRecordId 存在、日期未过、剩余号源 > 0。
- 去重校验：同一 patientId + scheduleRecordId 不允许重复（预约中 / 已预约）。
- 取消策略：仅允许从 预约中 / 已预约 → 已取消；是否回补号源按策略决定。
- 状态枚举：预约中、已预约、已取消、已过期、已就诊（预留）。

## 4. 统一错误响应
```json
{
  "code": 409,
  "message": "号源已满",
  "data": null
}
```
- 常见 code：400 参数错误；403 无权限；404 未找到；409 业务冲突；423 资源锁定；500 服务器错误。
