package com.example.Service;

import com.example.Conmon.result.Result;
import com.example.pojo.vo.HotDataPlatformVO;
import com.example.pojo.vo.StaticDataPlatformVO;

public interface DataPlatformService {

    /**
     * 获取静态数据
     * @return 静态数据
     */
    Result<StaticDataPlatformVO> getStaticData();
    
    /**
     * 获取实时数据
     * @return 实时数据
     */
    Result<HotDataPlatformVO> getHotData();
}