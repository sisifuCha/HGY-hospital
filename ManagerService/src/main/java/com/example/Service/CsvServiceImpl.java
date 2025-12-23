package com.example.Service;

import com.example.Mapper.CsvMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

@Service
public class CsvServiceImpl implements CsvService {

    @Autowired
    private CsvMapper adminMapper;

    @Override
    public void exportCSV(String type, String id, String begin, String end, HttpServletResponse response) {
        List<Map<String, Object>> dataList = null;
        String filename = "report.csv";

        switch (type) {
            case "DOC":
                dataList = adminMapper.getDoctorInfo();
                filename = "doctor_info.csv";
                break;
            case "PAT":
                dataList = adminMapper.getPatientInfo();
                filename = "patient_info.csv";
                break;
            case "REG":
                dataList = adminMapper.getRegistrationRecord(id, begin, end);
                filename = "registration_record.csv";
                break;
            case "SCH":
                dataList = adminMapper.getScheduleRecord(id, begin, end);
                filename = "schedule_record.csv";
                break;
            default:
                throw new IllegalArgumentException("Invalid type: " + type);
        }

        // 设置响应头
        try {
            // 设置文件名编码，防止中文乱码
            String encodedFilename = URLEncoder.encode(filename, "UTF-8");
            response.setContentType("text/csv;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFilename + "\"");
            response.setCharacterEncoding("UTF-8");

            // 创建PrintWriter
            PrintWriter writer = response.getWriter();
            
            // 写入BOM，解决中文乱码问题
            writer.write(new String(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF }));
            
            // 写入表头
            if (dataList != null && !dataList.isEmpty()) {
                Map<String, Object> firstRow = dataList.get(0);
                // 写入表头
                int i = 0;
                for (String key : firstRow.keySet()) {
                    if (i > 0) {
                        writer.write(",");
                    }
                    writer.write(escapeCsvValue(key));
                    i++;
                }
                writer.println();
                
                // 写入数据行
                for (Map<String, Object> row : dataList) {
                    int j = 0;
                    for (Object value : row.values()) {
                        if (j > 0) {
                            writer.write(",");
                        }
                        writer.write(escapeCsvValue(String.valueOf(value)));
                        j++;
                    }
                    writer.println();
                }
            }
            
            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 转义CSV值，处理包含逗号、引号或换行符的值
     */
    private String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }
        
        // 如果值包含逗号、引号或换行符，需要用引号包裹
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            // 转义值中的双引号为两个双引号
            value = value.replace("\"", "\"\"");
            // 用双引号包裹值
            value = "\"" + value + "\"";
        }
        
        return value;
    }
}