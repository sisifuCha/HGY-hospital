package com.example.Controller;

import com.example.Service.CsvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;

@RestController
public class CsvController {

    @Autowired
    private CsvService adminService;

    @GetMapping("/admin/getCSV")
    public void getCSV(@RequestParam(required = false) String type,
                       @RequestParam(required = false) String id,
                       @RequestParam(required = false) String begin,
                       @RequestParam(required = false) String end,
                       HttpServletResponse response) {
        if (type == null) {
             // Handle error or default
             return;
        }
        adminService.exportCSV(type, id, begin, end, response);
    }
}
