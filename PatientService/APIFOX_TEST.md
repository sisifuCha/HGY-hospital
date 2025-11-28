# Apifox 测试指南（候补挂号 & 患者加号申请）

## 快速准备
1. 启动后端：mvn spring-boot:run 或 ./gradlew bootRun（确保 application.yml 数据库配置正确）。
2. 初始化表：执行 hopital_new.sql（包含 patient_extra_apply 与 waiting_queue）。
3. 在 Apifox 新建 Environment，添加变量：
   - BASE_URL = http://localhost:8082
   - TOKEN = （如需鉴权）
   - TODAY = {{= new Date().toISOString().slice(0,10) }}
   - TEST_PATIENT_ID = 123
   - TEST_DEPT_ID = 10
   - TEST_DOCTOR_ID = 20

## 在 Apifox 中创建请求（复制到对应位置）

### Submit Extra Apply
- URL: {{BASE_URL}}/api/extra-apply
- Method: POST
- Headers: Content-Type: application/json ; Authorization: Bearer {{TOKEN}} （如需）
- Body (raw JSON):
{
  "patientId": {{TEST_PATIENT_ID}},
  "departmentId": {{TEST_DEPT_ID}},
  "doctorId": {{TEST_DOCTOR_ID}},
  "appointmentDate": "{{TODAY}}",
  "reason": "当天临时加号测试"
}
- Tests (粘到 Tests 标签):
pm.test("Status 201", () => pm.response.to.have.status(201));
const body = pm.response.json();
pm.test("has id", () => pm.expect(body.id).to.exist);
pm.test("status is PENDING", () => pm.expect(body.status).to.eql("PENDING"));
pm.test("locked is false", () => pm.expect(body.locked).to.be.false);
if (body.id) pm.environment.set("EXTRA_ID", body.id);

### Get Extra Apply By Id
- URL: {{BASE_URL}}/api/extra-apply/{{EXTRA_ID}}
- Method: GET
- Tests:
pm.test("Status 200", () => pm.response.to.have.status(200));
const b = pm.response.json();
pm.test("id match", () => pm.expect(b.id).to.eql(parseInt(pm.environment.get("EXTRA_ID"))));

### List By Patient
- URL: {{BASE_URL}}/api/extra-apply?patientId={{TEST_PATIENT_ID}}
- Method: GET
- Tests:
pm.test("Status 200", () => pm.response.to.have.status(200));
const arr = pm.response.json();
pm.test("contains created id", () => pm.expect(arr.some(r => r.id === parseInt(pm.environment.get("EXTRA_ID")))).to.be.true);

### Approve
- URL: {{BASE_URL}}/api/extra-apply/{{EXTRA_ID}}/approve
- Method: PUT
- Body: { "approverId": 999 } （可选）
- Tests:
pm.test("Status 200", () => pm.response.to.have.status(200));
const b = pm.response.json();
pm.test("status APPROVED", () => pm.expect(b.status).to.eql("APPROVED"));
pm.test("locked true", () => pm.expect(b.locked).to.be.true);

### Reject
- URL: {{BASE_URL}}/api/extra-apply/{{EXTRA_ID}}/reject
- Method: PUT
- Body: { "approverId": 999, "rejectReason": "测试驳回" }
- Tests:
pm.test("Status 200", () => pm.response.to.have.status(200));
const b = pm.response.json();
pm.test("status REJECTED", () => pm.expect(b.status).to.eql("REJECTED"));
pm.test("rejectReason set", () => pm.expect(b.rejectReason).to.eql("测试驳回"));

## 负向用例（建议）
- 缺字段 → 400（移除 reason/appointmentDate）
- appointmentDate 不是当天 → 400
- 重复申请 → 409 或 按实现断言
- 审批已处理 → 409/错误

## Runner 与并发
- 使用 Apifox Runner 做顺序回归。
- 大并发请使用 k6/JMeter，先在 DB 将 schedule left_source_count 置 0，再并发提交，查看 waiting_queue 写入情况。

## 常用 SQL
查记录：
SELECT id, patient_id, appointment_date, status, locked, reject_reason, created_at FROM patient_extra_apply WHERE id = <EXTRA_ID>;
候补查询：
SELECT * FROM waiting_queue WHERE patient_id = '{{TEST_PATIENT_ID}}' ORDER BY apply_time DESC LIMIT 10;
清理：
DELETE FROM patient_extra_apply WHERE patient_id = {{TEST_PATIENT_ID}} AND appointment_date = CURRENT_DATE;
DELETE FROM waiting_queue WHERE patient_id = '{{TEST_PATIENT_ID}}';

## 如果需要
- 我可以生成 Apifox 可导入的 Collection JSON（直接导入即可跑）。
- 或者生成 k6 并发脚本（用于候补压测）。

