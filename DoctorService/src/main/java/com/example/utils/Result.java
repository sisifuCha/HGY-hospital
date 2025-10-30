package com.example.utils;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 通用API响应结果封装类（Java Bean）
 * @param <T> 响应数据 (data) 的类型
 */
@Data // 自动生成 Getter, Setter, equals, hashCode, toString 方法
@NoArgsConstructor // 自动生成无参构造函数
@AllArgsConstructor // 自动生成全参构造函数
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 业务状态码 (非HTTP状态码) */
    private int code; 

    /** 消息/提示信息 */
    private String msg; 

    /** 实际返回的业务数据 */
    private T data; 
    
    // --- 静态工厂方法 (用于快速创建常用的 Result 对象) ---
    
    // 成功（带数据）
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    // 成功（不带数据）
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }
    
    // 成功（带数据和自定义消息）
    public static <T> Result<T> success(T data, String msg) {
        return new Result<>(200, msg, data);
    }
    
    // 登录成功（返回医生ID和JWT令牌）
    public static Result<Map<String, String>> loginSuccess(String doctorId, String token) {
        Map<String, String> loginData = new HashMap<>();
        loginData.put("doctorId", doctorId);
        loginData.put("token", token);
        return new Result<>(200, "登录成功", loginData);
    }

    // 失败/通用错误（自定义状态码和消息）
    public static <T> Result<T> fail(int code, String msg) {
        return new Result<>(code, msg, null);
    }
    
    // 失败/通用错误（使用默认的 HTTP 状态码作为业务码）
    public static <T> Result<T> fail(String msg) {
        // 通常使用 400 作为默认的业务失败码，表示请求处理失败
        return new Result<>(400, msg, null); 
    }

    // 检查业务是否成功
    public boolean isSuccess() {
        return this.code == 200;
    }

    // 将Result对象转换为Map，以便在Controller中进行统一响应 (可选，但很实用)
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("code", this.code);
        map.put("msg", this.msg);
        map.put("data", this.data);
        return map;
    }
}