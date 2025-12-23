package com.example.Service;

import jakarta.servlet.http.HttpServletResponse;

public interface CsvService {

    /**
     * 导出CSV报表
     * @param type 导出类型：DOC-医生信息，PAT-患者信息，REG-挂号记录，SCH-排班记录
     * @param id 筛选条件ID
     * @param begin 开始时间
     * @param end 结束时间
     * @param response 响应对象
     */
    void exportCSV(String type, String id, String begin, String end, HttpServletResponse response);
}