package com.example.conmon.exception;

import com.example.conmon.result.Result;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateRegistrationException.class)
    public Result<Void> handleDuplicate(DuplicateRegistrationException ex) {
        // Log the exception for debugging purposes
        System.err.println("Duplicate registration: " + ex.getMessage());
        return Result.fail(409, "重复挂号");
    }

    @ExceptionHandler(SourceFullException.class)
    public Result<Void> handleSourceFull(SourceFullException ex) {
        // Log the exception for debugging purposes
        System.err.println("Source full: " + ex.getMessage());
        return Result.fail(409, "号源已满");
    }

    @ExceptionHandler(CreateFailedException.class)
    public Result<Void> handleCreateFailed(CreateFailedException ex) {
        // Log the exception for debugging purposes
        System.err.println("Create failed: " + ex.getMessage());
        return Result.fail(500, "操作失败");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldError() != null
                ? ex.getBindingResult().getFieldError().getDefaultMessage()
                : "请求参数错误";
        return Result.fail(400, msg);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArg(IllegalArgumentException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "非法参数";
        
        // 记录不存在相关
        if (msg.contains("记录不存在") || msg.contains("排班记录不存在")) {
            return Result.fail(404, msg);
        }
        
        // 状态相关
        if (msg.contains("当前状态不可取消")) {
            return Result.fail(409, msg);
        }
        
        // 候补规则相关 - 返回具体错误信息
        if (msg.contains("候补人数已达上限") || msg.contains("该排班候补人数已达上限")) {
            return Result.fail(409, msg);  // 排班候补已满（MAX_WAITING_COUNT=100）
        }
        
        if (msg.contains("候补数量已达上限") || msg.contains("您的候补数量已达上限")) {
            return Result.fail(409, msg);  // 患者候补已满（MAX_PATIENT_WAITING=5）
        }
        
        if (msg.contains("已停止候补") || msg.contains("该排班已停止候补")) {
            return Result.fail(409, msg);  // 就诊前3小时停止候补（STOP_HOURS_BEFORE=3）
        }
        
        // 其他参数错误
        return Result.fail(400, msg);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleGeneral(Exception ex) {
        // Log the exception for debugging purposes
        System.err.println("General exception: " + ex.getMessage());
        return Result.fail(500, "服务器开小差，请稍后重试");
    }
}
