# 患者端订单管理API文档

## 概述
本文档描述患者端订单（支付）相关的REST API接口。

## ⚠️ 重要变更
**订单创建方式已变更**：订单不再由前端单独创建，而是在挂号成功时由后端自动生成。前端调用挂号接口即可同时完成挂号和订单创建。

## 基础URL
```
http://localhost:{port}/api
```

---

## 1. 创建挂号（自动生成订单）

### 接口描述
创建挂号记录，成功后系统会自动生成支付订单。订单金额会根据患者的报销类型自动计算实际支付金额。

### 请求
- **方法**: `POST`
- **路径**: `/api/registrations`
- **Content-Type**: `application/json`

### 请求体
```json
{
  "patientId": "PAT0001",
  "scheduleRecordId": "SCH20251218001"
}
```

### 响应（挂号成功）
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "patientId": "PAT0001",
    "scheduleRecordId": "SCH20251218001",
    "status": true,
    "paymentId": "uuid-string",
    "amount": 70.00,
    "registerTime": "2025-12-18T10:30:00.000+08:00"
  }
}
```

### 响应（号源不足）
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "patientId": "PAT0001",
    "scheduleRecordId": "SCH20251218001",
    "status": false
  }
}
```

### 字段说明
- `status`: 挂号是否成功（true=成功，false=号源不足）
- `paymentId`: 自动生成的订单ID（仅status=true时返回）
- `amount`: 实际应付金额（仅status=true时返回）
- `registerTime`: 挂号时间（仅status=true时返回）

### 计算逻辑
**实付金额 = 原价 × (1 - 报销比例/100)**

例如：原价100元，报销比例30%，则实付 = 100 × (1 - 0.3) = 70元

---

## 2. 订单列表

### 接口描述
获取指定患者的所有订单列表。

### 请求
- **方法**: `GET`
- **路径**: `/api/payments?patientId={patientId}`

### 查询参数
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| patientId | String | 是 | 患者ID |

### 响应
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 2,
    "payments": [
      {
        "paymentId": "uuid-1",
        "patientId": "PAT0001",
        "scheduleRecordId": "SCH001",
        "doctorName": "张医生",
        "departmentName": "内科",
        "payTime": "2025-12-18T10:30:00.000+08:00",
        "payStatus": "已支付",
        "oriAmount": 100.00,
        "askPayAmount": 70.00,
        "reimbursePercent": 30.00,
        "reimburseType": "职工医保"
      },
      {
        "paymentId": "uuid-2",
        "patientId": "PAT0001",
        "scheduleRecordId": "SCH002",
        "doctorName": "李医生",
        "departmentName": "外科",
        "payTime": "2025-12-17T14:20:00.000+08:00",
        "payStatus": "待支付",
        "oriAmount": 150.00,
        "askPayAmount": 150.00,
        "reimbursePercent": 0.00,
        "reimburseType": "自费"
      }
    ]
  }
}
```

### 订单状态说明
- `待支付`: 订单已创建，等待支付
- `已支付`: 订单支付完成
- `已取消`: 订单已取消

---

## 3. 订单详情

### 接口描述
查看指定订单的详细信息，包含原始金额、减免比例、减免后金额等。

### 请求
- **方法**: `GET`
- **路径**: `/api/payments/{paymentId}`

### 路径参数
| 参数 | 类型 | 说明 |
|------|------|------|
| paymentId | String | 订单ID |

### 响应
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "paymentId": "uuid-string",
    "patientId": "PAT0001",
    "scheduleRecordId": "SCH001",
    "doctorId": "DOC001",
    "doctorName": "张医生",
    "departmentName": "内科",
    "payTime": "2025-12-18T10:30:00.000+08:00",
    "payStatus": "已支付",
    "oriAmount": 100.00,
    "askPayAmount": 70.00,
    "reimbursePercent": 30.00,
    "reimburseType": "职工医保"
  }
}
```

### 字段说明
- `oriAmount`: 原始金额（挂号费）
- `reimbursePercent`: 报销比例（百分比）
- `askPayAmount`: 实际支付金额（减免后）
- `payTime`: 所有状态订单均返回此字段作为时间标识

---

## 4. 订单支付

### 接口描述
对待支付订单进行支付，模拟扣除患者医保余额。支付成功后自动更新挂号记录状态为"已挂号"。

### 请求
- **方法**: `POST`
- **路径**: `/api/payments/{paymentId}/pay`

### 路径参数
| 参数 | 类型 | 说明 |
|------|------|------|
| paymentId | String | 订单ID |

### 响应（成功）
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "paymentId": "uuid-string",
    "payStatus": "已支付",
    "payTime": "2025-12-18T10:35:00.000+08:00",
    "oriAmount": 100.00,
    "askPayAmount": 70.00,
    ...
  }
}
```

### 响应（失败）
```json
{
  "code": 400,
  "message": "医保余额不足，当前余额：50.00，需支付：70.00"
}
```

### 支付逻辑
1. 检查订单状态是否为"待支付"
2. 查询患者医保账户余额
3. 验证余额是否充足
4. 扣减医保余额
5. 更新订单状态为"已支付"
6. 更新挂号记录状态为"已挂号"

---

## 5. 取消订单

### 接口描述
取消指定订单。同时会取消关联的挂号记录，并根据订单状态决定是否回补号源。

### 请求
- **方法**: `DELETE`
- **路径**: `/api/payments/{paymentId}`

### 路径参数
| 参数 | 类型 | 说明 |
|------|------|------|
| paymentId | String | 订单ID |

### 响应
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "paymentId": "uuid-string",
    "payStatus": "已取消",
    "payTime": "2025-12-18T10:30:00.000+08:00",
    ...
  }
}
```

### 取消逻辑
1. 更新 `pay_record` 表中的 `pay_status` 为 `已取消`
2. 根据 `patient_id` 和 `sch_id` 更新 `register_record` 表中的 `status` 为 `已取消`
3. 如果订单原状态为"已支付"，回补号源（`left_source_count + 1`）

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误或业务逻辑错误 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 数据库表关系

### 相关表
1. **pay_record** - 支付记录表
2. **register_record** - 挂号记录表
3. **patient** - 患者表（关联报销类型和医保账户）
4. **reimburse_type** - 报销类型表（存储报销比例）
5. **medical_insurance** - 医保账户表（存储余额）
6. **doc_schedule_record** - 排班记录表
7. **doctor** - 医生表
8. **title_number_source** - 职称号源表（存储挂号费）

### 关联关系
```
pay_record.patient_id -> patient.id
patient.reimburse_id -> reimburse_type.id
patient.medical_insuranceid -> medical_insurance.id
pay_record.sch_id -> doc_schedule_record.id
pay_record.doc_id -> doctor.id
register_record (patient_id, sch_id) <-> pay_record (patient_id, sch_id)
```

---

## 前端集成示例

### Vue.js/Axios
```javascript
// 创建挂号（自动生成订单）
async createRegistration(patientId, scheduleRecordId) {
  const response = await axios.post('/api/registrations', {
    patientId,
    scheduleRecordId
  });
  return response.data;
}

// 获取订单列表
async getPayments(patientId) {
  const response = await axios.get('/api/payments', {
    params: { patientId }
  });
  return response.data;
}

// 查看订单详情
async getPaymentDetail(paymentId) {
  const response = await axios.get(`/api/payments/${paymentId}`);
  return response.data;
}

// 支付订单
async payOrder(paymentId) {
  const response = await axios.post(`/api/payments/${paymentId}/pay`);
  return response.data;
}

// 取消订单
async cancelOrder(paymentId) {
  const response = await axios.delete(`/api/payments/${paymentId}`);
  return response.data;
}
```

### 典型业务流程
```javascript
// 1. 患者挂号（自动创建订单）
const registrationResult = await createRegistration('PAT0001', 'SCH001');
if (registrationResult.data.status) {
  // 挂号成功，获得订单ID和金额
  const paymentId = registrationResult.data.paymentId;
  const amount = registrationResult.data.amount;
  
  // 2. 跳转到支付页面
  router.push({ 
    name: 'Payment', 
    params: { paymentId, amount } 
  });
  
  // 3. 用户确认后支付订单
  const payResult = await payOrder(paymentId);
  if (payResult.code === 200) {
    // 支付成功，跳转到成功页面
    router.push({ name: 'PaymentSuccess' });
  }
} else {
  // 号源不足，提示用户
  alert('号源已满，请选择其他时间段');
}
```
```javascript
// 创建挂号（自动生成订单）
async createRegistration(patientId, scheduleRecordId) {
  const response = await axios.post('/api/registrations', {
    patientId,
    scheduleRecordId
  });
  return response.data;
}

// 获取订单列表
async getPayments(patientId) {
  const response = await axios.get('/api/payments', {
    params: { patientId }
  });
  return response.data;
}

// 查看订单详情
async getPaymentDetail(paymentId) {
  const response = await axios.get(`/api/payments/${paymentId}`);
  return response.data;
}

// 支付订单
async payOrder(paymentId) {
  const response = await axios.post(`/api/payments/${paymentId}/pay`);
  return response.data;
}

// 取消订单
async cancelOrder(paymentId) {
  const response = await axios.delete(`/api/payments/${paymentId}`);
  return response.data;
}
```

### 典型业务流程
```javascript
// 1. 患者挂号（自动创建订单）
const registrationResult = await createRegistration('PAT0001', 'SCH001');
if (registrationResult.data.status) {
  // 挂号成功，获得订单ID和金额
  const paymentId = registrationResult.data.paymentId;
  const amount = registrationResult.data.amount;
  
  // 2. 跳转到支付页面
  router.push({ 
    name: 'Payment', 
    params: { paymentId, amount } 
  });
  
  // 3. 用户确认后支付订单
  const payResult = await payOrder(paymentId);
  if (payResult.code === 200) {
    // 支付成功，跳转到成功页面
    router.push({ name: 'PaymentSuccess' });
  }
} else {
  // 号源不足，提示用户
  alert('号源已满，请选择其他时间段');
}
```

---

## 注意事项

1. **订单创建时机**: ⚠️ 订单在挂号成功时自动创建，前端不需要单独调用创建订单接口
2. **挂号返回字段**: 挂号成功后会返回 `paymentId` 和 `amount`，前端可直接使用
3. **支付幂等性**: 已支付或已取消的订单不能再次支付
4. **医保余额**: 支付前必须验证医保余额充足
5. **事务一致性**: 支付成功必须同时更新订单状态和挂号状态
6. **号源管理**: 取消已支付订单时需要回补号源
7. **时间字段**: 所有状态的订单都返回 `payTime` 字段，未支付时为创建时间，已支付时为支付时间
8. **候补转正**: 当有患者取消挂号且有候补队列时，候补患者自动转正并生成订单
