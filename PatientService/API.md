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
  - `departmentId` (string, required)
  - `date` (string, required, YYYY-MM-DD)
- Success Response (200):
  ```json
  [
    {
      "doctorId": "DOC001",
      "doctorName": "张三",
      "doctorTitle": "主任医师",
      "specialty": "高血压、冠心病等心血管疾病",
      "schedules": [
        {
          "scheduleId": "101",
          "timePeriodName": "上午",
          "startTime": "08:00:00",
          "endTime": "12:00:00",
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
    "id": "DOC001",
    "name": "张三",
    "docTitleId": "TITLE001",
    "title": "主任医师",
    "specialty": "心血管相关疾病",
    "details": "从业20年，经验丰富",
    "departId": "DEP101",
    "clinicId": "CLIN001",
    "status": "在职"
  }
  ```

### 1.4 根据账号获取患者ID（新增）
- URL: `/user/patient-id`
- Method: `GET`
- Query Params:
  - `account` (string, required)
- Success Response (200):
  ```json
  {
    "code": 200,
    "msg": "success",
    "data": "PAT0001"
  }
  ```
- Error Response (示例):
  - 账号为空：
    ```json
    { "code": 400, "msg": "账号不能为空", "data": null }
    ```
  - 用户不存在：
    ```json
    { "code": 404, "msg": "用户不存在", "data": null }
    ```
  - 非患者账号：
    ```json
    { "code": 400, "msg": "该账号不是患者类型", "data": null }
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
    "patientId": "PAT0001",
    "scheduleRecordId": "SCH7890",
    "confirm": true
  }
  ```
- 字段说明：
  - `confirm`: true → 已预约；false → 预约中。
- Success Response (200):
  ```json
  {
    "patientId": "PAT0001",
    "scheduleRecordId": "SCH7890",
    "registerTime": "2025-11-15 09:30:12",
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
        "patientId": "PAT0001",
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
    "patientId": "PAT0001",
    "scheduleRecordId": "SCH7890",
    "registerTime": "2025-11-15 09:30:12",
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
    "patientId": "PAT0001",
    "scheduleRecordId": "SCH7890",
    "status": "已取消"
  }
  ```

### 2.5 读取单条挂号（按挂号ID）
- URL: `/api/registrations/{registrationId}`
- Method: `GET`
- Path Params:
  - `registrationId` (string, required)  // 对应数据库表 `register_record` 的主键（或唯一标识）
- Description: 按挂号表的内部 ID 读取单条挂号记录，适用于前端在列表中点击某条记录查看详情的场景。与 `/api/registrations/by-key` 不同，本接口以挂号表的主键为索引，便于运维和内部引用。请求应校验调用者对该 patientId 的访问权限（仅患者本人或有权限的管理/业务系统）。
- Success Response (200):
  ```json
  {
    "registrationId": "REG0123",
    "patientId": "PAT0001",
    "scheduleRecordId": "SCH7890",
    "registerTime": "2025-11-15T09:30:12",
    "status": "已预约",
    "doctorId": "DOC0023",
    "departmentId": "DEP005",
    "clinicId": "CLIN001",
    "scheduleDate": "2025-11-15",
    "timePeriodName": "上午",
    "registrationFee": 50,
    "paymentStatus": "已支付"
  }
  ```
- 注：`paymentStatus` 为可选字段，根据系统是否关联合并支付信息返回。
- Error Responses (examples):
  - 找不到记录：
    ```json
    { "code": 404, "message": "挂号记录未找到", "data": null }
    ```
  - 无权访问（尝试访问非本人记录）：
    ```json
    { "code": 403, "message": "无权访问该挂号记录", "data": null }
    ```
  - 参数错误：
    ```json
    { "code": 400, "message": "registrationId 不能为空", "data": null }
    ```
- Notes / 校验要点:
  - 如果系统没有单独的 `registrationId` 字段，可由 `patientId + scheduleRecordId` 唯一定位；本接口可返回同样的复合键字段以兼容现有设计。
  - 必须校验记录的归属（patientId）以防信息泄露。
  - 返回字段尽量与前端所需字段对齐，必要时可扩展（例如医生简介、诊室名称、是否可取消等）。

### 2.6 上传患者个人档案
- URL: `/api/patients/{patientId}/profile`
- Method: `POST`
- Path Params:
  - `patientId` (string, required)  // 患者唯一标识
- Request Body:
  ```json
  {
    "name": "张三",
    "gender": "男",
    "birthDate": "1985-06-15",
    "phone": "13800138000",
    "address": "北京市朝阳区某小区",
    "medicalHistory": "无",
    "allergies": "青霉素"
  }
  ```
- Description: 上传或更新患者的个人档案信息。调用者需校验对该 patientId 的访问权限。
- Success Response (200):
  ```json
  {
    "code": 200,
    "message": "档案上传成功",
    "data": null
  }
  ```
- Error Responses (examples):
  - 参数错误：
    ```json
    { "code": 400, "message": "请求参数不合法", "data": null }
    ```
  - 无权访问：
    ```json
    { "code": 403, "message": "无权访问该患者档案", "data": null }
    ```
  - 患者不存在：
    ```json
    { "code": 404, "message": "患者不存在", "data": null }
    ```

### 2.7 读取患者个人档案
- URL: `/api/patients/{patientId}/profile`
- Method: `GET`
- Path Params:
  - `patientId` (string, required)  // 患者唯一标识
- Description: 读取患者的个人档案信息。调用者需校验对该 patientId 的访问权限。
- Success Response (200):
  ```json
  {
    "patientId": "PAT0001",
    "name": "张三",
    "gender": "男",
    "birthDate": "1985-06-15",
    "phone": "13800138000",
    "address": "北京市朝阳区某小区",
    "medicalHistory": "无",
    "allergies": "青霉素"
  }
  ```
- Error Responses (examples):
  - 无权访问：
    ```json
    { "code": 403, "message": "无权访问该患者档案", "data": null }
    ```
  - 患者不存在：
    ```json
    { "code": 404, "message": "患者不存在", "data": null }
    ```
  - 参数错误：
    ```json
    { "code": 400, "message": "patientId 不能为空", "data": null }
    ```

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
