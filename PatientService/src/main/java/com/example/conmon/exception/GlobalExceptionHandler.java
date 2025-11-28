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
        String raw = ex.getMessage() != null ? ex.getMessage() : "";
        if ("记录不存在".equals(raw)) {
            return Result.fail(404, "未找到");
        }
        if ("当前状态不可取消".equals(raw)) {
            return Result.fail(409, "状态不允许操作");
        }
        return Result.fail(400, "非法参数");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleGeneral(Exception ex) {
        // Log the exception for debugging purposes
        System.err.println("General exception: " + ex.getMessage());
        return Result.fail(500, "服务器开小差，请稍后重试");
    }
}
