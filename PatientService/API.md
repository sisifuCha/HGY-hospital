# 接口文档

## 1. 挂号相关

### 1.1 获取科室列表

- **URL**: `/api/departments`
- **Method**: `GET`
- **Description**: 获取所有一级科室及其下的二级科室列表。
- **Success Response (200)**:
  ```json
  [
    {
      "id": 1,
      "name": "内科",
      "subDepartments": [
        { "id": 101, "name": "呼吸内科" },
        { "id": 102, "name": "心血管内科" }
      ]
    },
    {
      "id": 2,
      "name": "外科",
      "subDepartments": [
        { "id": 201, "name": "骨科" },
        { "id": 202, "name": "普外科" }
      ]
    }
  ]
  ```

### 1.2 根据科室和日期获取医生排班

- **URL**: `/api/registration/doctors`
- **Method**: `GET`
- **Params**:
  - `departmentId` (number, required): 二级科室ID.
  - `date` (string, required): 日期, 格式 `YYYY-MM-DD`.
- **Success Response (200)**:
  ```json
  [
    {
      "doctorId": 1,
      "doctorName": "张三",
      "doctorTitle": "主任医师",
      "specialty": "高血压、冠心病等心血管疾病",
      "schedules": [
        {
          "scheduleId": 101,
          "timePeriodName": "上午",
          "startTime": "08:00",
          "endTime": "12:00",
          "registrationFee": 50,
          "leftSourceCount": 5
        },
        {
          "scheduleId": 102,
          "timePeriodName": "下午",
          "startTime": "14:00",
          "endTime": "18:00",
          "registrationFee": 50,
          "leftSourceCount": 0
        }
      ]
    }
  ]
  ```

