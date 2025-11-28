package com.example.Service;

import com.example.Mapper.PatientMapper;
import com.example.Mapper.UserMapper;
import com.example.conmon.result.Result;
import com.example.pojo.dto.LoginRequest;
import com.example.pojo.dto.RegisterRequest;
import com.example.pojo.entity.Patient;
import com.example.pojo.entity.User;
import com.example.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PatientMapper patientMapper;

    @Autowired
    private JwtUtil jwtUtil;


    @Override
    public Result<String> login(LoginRequest loginRequest) {
        // 1. 根据账号查询用户
        User user = userMapper.findByAccount(loginRequest.getAccount());

        // 2. 判断用户是否存在
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 3. 验证密码
        if (!user.getUserPassword().equals(loginRequest.getPassword())) {
            return Result.error("密码错误");
        }

        // 4. 密码正确，生成JWT令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("userAccount", user.getUserAccount());
        String token = jwtUtil.generateToken(claims);

        // 5. 返回成功结果，包含token
        return Result.success(token);
    }

    @Override
    @Transactional
    public Result<String> register(RegisterRequest registerRequest) {
        // 1. 检查身份证号是否已存在
        if (patientMapper.countByIdentificationId(registerRequest.getIdentificationId()) > 0) {
            return Result.error("该身份证号已被注册");
        }

        // 1.1 检查身份证号长度
        if (registerRequest.getIdentificationId() == null || registerRequest.getIdentificationId().length() != 18) {
            return Result.error("身份证号必须为18位");
        }

        // 2. 检查用户是否已存在
        User existingUser = userMapper.findByAccount(registerRequest.getUserAccount());
        if (existingUser != null) {
            return Result.error("用户已存在");
        }

        // 3. 创建新用户对象
        User newUser = new User();

        // 3.1 生成新的患者ID
        String maxPatId = userMapper.findMaxPatId();
        int newIdNum = 1;
        if (maxPatId != null && maxPatId.startsWith("PAT")) {
            try {
                newIdNum = Integer.parseInt(maxPatId.substring(3)) + 1;
            } catch (NumberFormatException e) {
                // 处理潜在的解析错误，例如当ID格式不规范时
                // 在这里可以记录日志，或者根据业务需求进行其他处理
                // 为了简单起见，我们仍然从1开始
            }
        }
        String newPatId = String.format("PAT%04d", newIdNum);
        newUser.setUserId(newPatId);

        newUser.setUserAccount(registerRequest.getUserAccount());
        newUser.setUserPassword(registerRequest.getUserPassword());
        newUser.setUserName(registerRequest.getUserName());
        newUser.setUserGender(registerRequest.getUserGender());
        newUser.setUserEmail(registerRequest.getUserEmail());
        newUser.setUserPhone(registerRequest.getUserPhone());
        // 设置用户类型为患者
        newUser.setUserType("PAT");

        // 4. 插入数据库
        userMapper.insert(newUser);

        // 5. 创建Patient对象并插入数据库
        Patient newPatient = new Patient();
        newPatient.setPatientId(newPatId);
        newPatient.setBirthday(registerRequest.getBirthday());
        newPatient.setIdentificationId(registerRequest.getIdentificationId());
        patientMapper.insert(newPatient);


        // 6. 返回成功结果
        return Result.success("注册成功");
    }
}
