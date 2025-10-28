package com.example.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.pojo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 用户数据访问层
 * 继承自 Mybatis-Plus 的 BaseMapper，提供了基础的 CRUD 功能
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据登录账号查询用户信息
     * @param userAccount 登录账号
     * @return 匹配的用户实体，如果不存在则返回 null
     */
    @Select("SELECT * FROM \"user\" WHERE account = #{userAccount}")
    User findByUserAccount(String userAccount);

}

