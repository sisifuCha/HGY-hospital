package com.example.Controller;

import com.example.Conmon.result.Result;
import com.example.Service.DataPlatformService;
import com.example.pojo.vo.HotDataPlatformVO;
import com.example.pojo.vo.StaticDataPlatformVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class DataPlatformController {

    @Autowired
    private DataPlatformService dataPlatformService;

    @GetMapping("/getStaticData")
    public Result<StaticDataPlatformVO> getStaticData() {
        return dataPlatformService.getStaticData();
    }
    
    @GetMapping("/getHotData")
    public Result<HotDataPlatformVO> getHotData() {
        return dataPlatformService.getHotData();
    }
}