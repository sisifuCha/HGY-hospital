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
     * @param account 登录账号
     * @return 匹配的用户实体，如果不存在则返回 null
     */
    User findByAccount(String account);

    /**
     * 根据邮箱查询用户信息
     * @param email 邮箱
     * @return 用户实体，不存在返回 null
     */
    User findByEmail(String email);

    /**
     * 更新用户密码（明文存储：与现有登录/注册保持一致）
     * @param userId 用户ID
     * @param newPassword 新密码
     * @return 影响行数
     */
    int updatePasswordByUserId(String userId, String newPassword);

    /**
     * 查询患者类型的最大ID
     * @return 最大ID字符串，如 "PAT0012"
     */
    @Select("SELECT max(id) FROM \"user\" WHERE user_type = 'PAT'")
    String findMaxPatId();
}
