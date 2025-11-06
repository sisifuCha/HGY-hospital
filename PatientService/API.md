# 患者端 API 接口文档

## 基础信息
- **基地址 (Base URL)**: `http://localhost:8082`
- **数据格式**: 所有请求和响应的数据格式均为 `application/json`。
- **认证**: (当前接口暂无认证，后续可扩展)

---

## 1. 获取医生排班列表

根据指定的科室ID和日期，获取该科室下所有医生的列表以及他们当天的排班情况。

- **功能**: 查询医生排班
- **HTTP 方法**: `GET`
- **URL**: `/api/registration/doctors`
- **请求参数 (Query Parameters)**:

| 参数名 | 类型 | 是否必须 | 描述 | 示例 |
| :--- | :--- | :--- | :--- | :--- |
| `departmentId` | string | 是 | 科室的唯一ID | `dep-1` |
| `date` | string | 是 | 查询的日期，格式为 `YYYY-MM-DD` | `2025-11-08` |

- **成功响应 (200 OK)**:
  返回一个医生排班信息对象的数组。

  **示例**:
  ```json
  [
    {
      "doctorId": "doc-101",
      "doctorName": "张三丰",
      "doctorTitle": "主任医师",
      "specialty": "内科常见病、多发病诊治",
      "schedules": [
        {
          "scheduleId": "sch-201",
          "timePeriodName": "上午",
          "startTime": "08:00:00",
          "endTime": "12:00:00",
          "leftSourceCount": 10
        },
        {
          "scheduleId": "sch-202",
          "timePeriodName": "下午",
          "startTime": "14:00:00",
          "endTime": "17:30:00",
          "leftSourceCount": 5
        }
      ]
    },
    {
      "doctorId": "doc-102",
      "doctorName": "李时珍",
      "doctorTitle": "副主任医师",
      "specialty": "疑难杂症诊断",
      "schedules": [
        {
          "scheduleId": "sch-203",
          "timePeriodName": "上午",
          "startTime": "08:30:00",
          "endTime": "11:30:00",
          "leftSourceCount": 0
        }
      ]
    }
  ]
  ```

---

## 2. 获取医生详细信息

根据医生ID，获取该医生的详细个人信息，如履历、专长等。

- **功能**: 查询医生详情
- **HTTP 方法**: `GET`
- **URL**: `/api/registration/doctor/{id}`
- **路径参数 (Path Variable)**:

| 参数名 | 类型 | 描述 |
| :--- | :--- | :--- |
| `id` | string | 医生的唯一ID |

- **成功响应 (200 OK)**:
  返回一个包含医生详细信息的对象。

  **示例**:
  ```json
  {
    "id": "doc-101",
    "name": "张三丰",
    "sex": "男",
    "docTitleId": "title-1",
    "status": "在职",
    "clinicId": "clinic-1",
    "details": "张三丰医生，拥有超过20年的临床经验，是国内知名的内科专家...",
    "specialty": "内科常见病、多发病诊治，尤其擅长心血管系统疾病。",
    "departId": "dep-1"
  }
  ```

---

## 通用错误响应

当请求出错时（如参数错误、资源未找到等），后端会返回统一格式的错误信息。

- **错误响应示例 (404 Not Found)**:
  ```json
  {
    "timestamp": "2025-11-07T10:15:30.123Z",
    "status": 404,
    "error": "Not Found",
    "message": "Doctor with id doc-999 not found",
    "path": "/api/registration/doctor/doc-999"
  }
  ```
