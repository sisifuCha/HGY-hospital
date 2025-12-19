# 前端接口规则验证文档

## 📋 接口返回信息测试

### 1️⃣ 挂号接口 - `/api/registrations` (POST)

#### ✅ 成功场景
**请求:**
```json
{
  "patientId": "PAT0001",
  "scheduleRecordId": "SCH001"
}
```

**响应 (200 OK):**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "patientId": "PAT0001",
    "scheduleRecordId": "SCH001",
    "status": true,
    "paymentId": "PAY20231218001",
    "amount": 50.00,
    "registerTime": "2025-12-18T20:30:00"
  }
}
```

#### ❌ 失败场景1: 重复挂号
**响应 (409 Conflict):**
```json
{
  "code": 409,
  "message": "重复挂号",
  "data": null
}
```

#### ❌ 失败场景2: 号源不足
**响应 (200 OK - 但status=false):**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "patientId": "PAT0002",
    "scheduleRecordId": "SCH_NO_SOURCE",
    "status": false,
    "paymentId": null,
    "amount": null
  }
}
```
> **注意**: 号源为0时不抛异常，而是返回status=false，前端需要判断此字段提示用户"号源已满，请候补"

---

### 2️⃣ 候补接口 - `/api/registrations/waiting` (POST)

#### ✅ 成功场景
**请求:**
```json
{
  "patientId": "PAT0019",
  "scheduleRecordId": "SCH_NO_SOURCE"
}
```

**响应 (200 OK):**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "waitingId": "WAIT-UUID-001",
    "patientId": "PAT0019",
    "scheduleRecordId": "SCH_NO_SOURCE",
    "status": "候补中",
    "position": 1,
    "waitingTime": "2025-12-18T20:30:00"
  }
}
```

#### ❌ 失败场景1: 号源未满（有号源时不能候补）
**响应 (409 Conflict):**
```json
{
  "code": 409,
  "message": "号源已满",
  "data": null
}
```
> **规则**: 只有当`left_source_count = 0`时才能候补

#### ❌ 失败场景2: 排班候补人数已达上限
**请求:** 当前排班已有100人候补
**响应 (409 Conflict):**
```json
{
  "code": 409,
  "message": "该排班候补人数已达上限",
  "data": null
}
```
> **规则**: `MAX_WAITING_COUNT = 100`（单个排班最多100人候补）

#### ❌ 失败场景3: 患者候补数量已达上限
**请求:** 该患者已有5个候补
**响应 (409 Conflict):**
```json
{
  "code": 409,
  "message": "您的候补数量已达上限",
  "data": null
}
```
> **规则**: `MAX_PATIENT_WAITING = 5`（单个患者最多5个候补）

#### ❌ 失败场景4: 就诊前3小时停止候补
**请求:** 当前时间距离就诊时间不足3小时
**响应 (409 Conflict):**
```json
{
  "code": 409,
  "message": "该排班已停止候补",
  "data": null
}
```
> **规则**: `STOP_HOURS_BEFORE = 3`（就诊前3小时停止候补）

#### ❌ 失败场景5: 重复候补
**响应 (409 Conflict):**
```json
{
  "code": 409,
  "message": "重复挂号",
  "data": null
}
```

#### ❌ 失败场景6: 排班不存在
**响应 (404 Not Found):**
```json
{
  "code": 404,
  "message": "排班记录不存在",
  "data": null
}
```

---

### 3️⃣ 查询排班信息 - `/api/registration/doctors` (GET)

**请求参数:**
```
GET /api/registration/doctors?departmentId=DEP001&date=2025-12-20
```

**响应 (200 OK):**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "doctorId": "DOC0007",
      "doctorName": "张医生",
      "schedules": [
        {
          "scheduleId": "SCH001",
          "scheduleDate": "2025-12-20",
          "leftSourceCount": 10,
          "waitingCount": 0,
          "canRegister": true,
          "canWaiting": false,
          "waitingClosed": false
        },
        {
          "scheduleId": "SCH_NO_SOURCE",
          "scheduleDate": "2025-12-24",
          "leftSourceCount": 0,
          "waitingCount": 25,
          "canRegister": false,
          "canWaiting": true,
          "waitingClosed": false
        }
      ]
    }
  ]
}
```

**字段说明:**
- `leftSourceCount`: 剩余号源数量
- `waitingCount`: 当前候补人数
- `canRegister`: 是否可以直接挂号（号源>0）
- `canWaiting`: 是否可以候补（号源=0 且 候补未满 且 未截止）
- `waitingClosed`: 候补是否已截止（就诊前3小时）

**前端展示逻辑:**
```javascript
if (schedule.canRegister) {
  // 显示"挂号"按钮，剩余号源: ${leftSourceCount}
} else if (schedule.canWaiting) {
  // 显示"候补"按钮，当前候补: ${waitingCount}人
} else if (schedule.waitingClosed) {
  // 显示"已截止"（灰色不可点击）
} else {
  // 显示"候补已满"（灰色不可点击）
}
```

---

### 4️⃣ 查询候补列表 - `/api/registrations/waiting` (GET)

**请求参数:**
```
GET /api/registrations/waiting?scheduleRecordId=SCH_NO_SOURCE
```

**响应 (200 OK):**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "scheduleRecordId": "SCH_NO_SOURCE",
    "waitingCount": 3,
    "waitingList": [
      {
        "waitingId": "WAIT-001",
        "patientId": "PAT0019",
        "position": 1,
        "status": "候补中",
        "waitingTime": "2025-12-18T10:00:00"
      },
      {
        "waitingId": "WAIT-002",
        "patientId": "PAT0020",
        "position": 2,
        "status": "候补中",
        "waitingTime": "2025-12-18T10:05:00"
      },
      {
        "waitingId": "WAIT-003",
        "patientId": "PAT0021",
        "position": 3,
        "status": "候补中",
        "waitingTime": "2025-12-18T10:10:00"
      }
    ]
  }
}
```

**字段说明:**
- `position`: 候补位置（按`waiting_time`排序的FIFO队列）
- `status`: 候补状态（候补中/已转正/已取消）

---

### 5️⃣ 取消挂号 - `/api/registrations/{registrationId}` (DELETE)

#### ✅ 成功场景 - 有候补患者自动转正

**响应 (200 OK):**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "cancelled": true,
    "refundAmount": 50.00,
    "refundRate": 1.00,
    "promotedPatient": {
      "patientId": "PAT0019",
      "registrationId": "REG-NEW-001",
      "paymentId": "PAY-NEW-001"
    }
  }
}
```

**业务逻辑:**
1. 取消挂号
2. 查询候补队列（按`waiting_time`排序）
3. 自动转正第一个候补患者
4. 更新候补状态为"已转正"
5. 创建新的挂号和支付订单
6. 发送转正通知消息

#### ✅ 成功场景 - 无候补患者号源回补

**响应 (200 OK):**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "cancelled": true,
    "refundAmount": 30.00,
    "refundRate": 0.60,
    "sourceRestored": true
  }
}
```

**业务逻辑:**
1. 取消挂号
2. 查询候补队列，发现无候补
3. `left_source_count += 1`（号源回补）

---

### 6️⃣ 退款规则验证

#### 退款比例计算

| 取消时间 | 退款比例 | 示例（挂号费50元） |
|---------|---------|------------------|
| 提前24小时以上 | 100% | 退款50元 |
| 提前12-24小时 | 80% | 退款40元 |
| 提前6-12小时 | 50% | 退款25元 |
| 提前3-6小时 | 30% | 退款15元 |
| 提前1-3小时 | 10% | 退款5元 |
| 不足1小时 | 0% | 不退款 |

**数据库配置表:** `refund_rate`
```sql
SELECT hours_before, refund_rate, description 
FROM refund_rate 
ORDER BY sort_order;

-- 结果:
-- 24.00 | 1.00 | 提前1天以上全额退款
-- 12.00 | 0.80 | 提前0.5天退款80%
-- 6.00  | 0.50 | 提前6小时退款50%
-- 3.00  | 0.30 | 提前3小时退款30%
-- 1.00  | 0.10 | 提前1小时退款10%
-- 0.00  | 0.00 | 1小时内不予退款
```

---

## 🎯 前端集成建议

### 1. 挂号按钮逻辑
```javascript
async function handleRegister(scheduleId) {
  try {
    const response = await axios.post('/api/registrations', {
      patientId: currentUser.id,
      scheduleRecordId: scheduleId
    });
    
    if (response.data.code === 200) {
      const data = response.data.data;
      
      if (data.status === true) {
        // 挂号成功
        showSuccess(`挂号成功！请在30分钟内支付订单`);
        redirectToPayment(data.paymentId);
      } else {
        // 号源不足，引导候补
        showConfirm('号源已满，是否加入候补队列？', () => {
          handleWaiting(scheduleId);
        });
      }
    }
  } catch (error) {
    if (error.response?.status === 409) {
      showError(error.response.data.message); // "重复挂号"
    }
  }
}
```

### 2. 候补按钮逻辑
```javascript
async function handleWaiting(scheduleId) {
  try {
    const response = await axios.post('/api/registrations/waiting', {
      patientId: currentUser.id,
      scheduleRecordId: scheduleId
    });
    
    if (response.data.code === 200) {
      const data = response.data.data;
      showSuccess(`候补成功！您的候补位置: ${data.position}`);
    }
  } catch (error) {
    if (error.response?.status === 409) {
      const msg = error.response.data.message;
      
      if (msg.includes('候补人数已达上限')) {
        showError('该排班候补已满（最多100人）');
      } else if (msg.includes('您的候补数量已达上限')) {
        showError('您的候补数量已达上限（最多5个）');
      } else if (msg.includes('已停止候补')) {
        showError('就诊前3小时停止候补');
      } else {
        showError(msg);
      }
    }
  }
}
```

### 3. 排班列表展示
```javascript
function renderSchedule(schedule) {
  if (schedule.canRegister) {
    return `
      <button onclick="handleRegister('${schedule.scheduleId}')">
        挂号 (剩余 ${schedule.leftSourceCount})
      </button>
    `;
  } else if (schedule.canWaiting) {
    return `
      <button onclick="handleWaiting('${schedule.scheduleId}')">
        候补 (${schedule.waitingCount}人候补中)
      </button>
    `;
  } else if (schedule.waitingClosed) {
    return `<span class="disabled">已截止</span>`;
  } else {
    return `<span class="disabled">候补已满</span>`;
  }
}
```

---

## ✅ 规则验证总结

| 规则类型 | 规则名称 | 配置值 | 前端提示 | HTTP状态 |
|---------|---------|--------|----------|----------|
| 候补规则 | MAX_WAITING_COUNT | 100 | "该排班候补人数已达上限" | 409 |
| 候补规则 | MAX_PATIENT_WAITING | 5 | "您的候补数量已达上限" | 409 |
| 候补规则 | STOP_HOURS_BEFORE | 3 | "该排班已停止候补" | 409 |
| 挂号规则 | 号源检查 | left_source > 0 | status=false（需前端判断） | 200 |
| 挂号规则 | 重复检查 | 唯一约束 | "重复挂号" | 409 |
| 退款规则 | 24小时以上 | 100% | "全额退款50元" | 200 |
| 退款规则 | 12-24小时 | 80% | "退款40元（80%）" | 200 |
| 退款规则 | 6-12小时 | 50% | "退款25元（50%）" | 200 |
| 退款规则 | 3-6小时 | 30% | "退款15元（30%）" | 200 |
| 退款规则 | 1-3小时 | 10% | "退款5元（10%）" | 200 |
| 退款规则 | 不足1小时 | 0% | "不予退款" | 200 |
| 转正规则 | FIFO队列 | waiting_time排序 | "候补转正成功" | 自动触发 |

---

## 🔍 测试验证清单

- [x] 挂号成功返回paymentId和amount
- [x] 号源不足返回status=false
- [x] 重复挂号返回409错误
- [x] 候补成功返回position位置
- [x] 排班候补超100人返回特定错误
- [x] 患者候补超5个返回特定错误
- [x] 就诊前3小时候补返回特定错误
- [x] 取消挂号自动转正候补患者
- [x] 取消挂号无候补时号源回补
- [x] 退款比例根据时间正确计算
- [x] 查询排班返回canRegister/canWaiting状态

**所有规则都会通过GlobalExceptionHandler返回正确的HTTP状态码和错误信息！** ✅
