package com.example.Service;

import jakarta.servlet.http.HttpServletResponse;

public interface CsvService {
    void exportCSV(String type, String id, String begin, String end, HttpServletResponse response);
}
