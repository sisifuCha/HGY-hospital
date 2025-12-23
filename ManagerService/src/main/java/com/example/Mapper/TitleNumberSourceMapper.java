package com.example.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

public interface TitleNumberSourceMapper extends BaseMapper {

    @Select("SELECT id FROM title_number_source WHERE name = #{name}")
    String getIdByName(String name);
}