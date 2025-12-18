package com.example.Mapper;

import com.example.pojo.entity.RefundRate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface RefundMapper {
    
    /**
     * 获取所有退款规则（按 sort_order 排序）
     */
    List<RefundRate> getAllRefundRates();
    
    /**
     * 根据提前小时数获取退款比例
     */
    BigDecimal getRefundRateByHours(@Param("hoursBefore") BigDecimal hoursBefore);
}
