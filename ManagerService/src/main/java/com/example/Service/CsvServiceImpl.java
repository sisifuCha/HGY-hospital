package com.example.Service;

import com.example.Mapper.CsvMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter writer = response.getWriter()) {
            if (dataList != null && !dataList.isEmpty()) {
                // Write header
                Map<String, Object> firstRow = dataList.get(0);
                Set<String> headers = firstRow.keySet();
                writer.println(String.join(",", headers));

                // Write data
                for (Map<String, Object> row : dataList) {
                    StringBuilder sb = new StringBuilder();
                    int i = 0;
                    for (String header : headers) {
                        Object value = row.get(header);
                        sb.append(value != null ? value.toString() : "");
                        if (i < headers.size() - 1) {
                            sb.append(",");
                        }
                        i++;
                    }
                    writer.println(sb.toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
