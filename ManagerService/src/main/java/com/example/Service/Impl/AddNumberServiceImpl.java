package com.example.Service.Impl;

import com.example.Conmon.result.Result;
import com.example.Mapper.AddNumberMapper;
import com.example.Service.AddNumberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AddNumberServiceImpl implements AddNumberService {
    @Autowired
    private AddNumberMapper addNumberMapper;

    @Override
    public Result<Map<String, Object>> getAddedSource(Integer pageSize, Integer page) {
        // 设置默认值
        if (pageSize == null || pageSize <= 0) {
            pageSize = 10;
        }
        if (page == null || page <= 0) {
            page = 1;
        }

        // 计算偏移量
        int offset = (page - 1) * pageSize;

        // 查询数据
        List<Map<String, Object>> addedSourceList = addNumberMapper.getAddedSource(pageSize, offset);
        // 获取总记录数
        int total = addNumberMapper.getAddedSourceCount();

        // 封装返回结果
        Map<String, Object> data = new HashMap<>();
        data.put("AddedSourceList", addedSourceList);
        data.put("total", total);

        return Result.success(data);
    }

    @Transactional
    @Override
    public Result<String> checkAddedSource(String patientId, String schId, String status) {
        // 参数校验
        if (patientId == null || schId == null || status == null) {
            return Result.fail("参数不能为空");
        }

        // 更新加号申请状态
        int updateResult = addNumberMapper.updateAddedSourceStatus(patientId, schId, status);
        if (updateResult <= 0) {
            return Result.fail("更新加号申请状态失败");
        }

        // 如果状态为"已同意"，则在挂号表中新增对应的条目
        if ("已同意".equals(status)) {
            int addResult = addNumberMapper.addRegisterRecord(patientId, schId);
            if (addResult <= 0) {
                return Result.fail("新增挂号记录失败");
            }
        }

        return Result.success("审批成功");
    }
}