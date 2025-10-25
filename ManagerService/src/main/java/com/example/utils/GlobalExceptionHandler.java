//package com.example.utils;
//
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//@RestControllerAdvice
//public class GlobalExceptionHandler {
//
//    /**
//     * 处理参数校验异常
//     */
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public Result<String> handleValidationException(MethodArgumentNotValidException e) {
//        String errorMessage = e.getBindingResult().getAllErrors().stream()
//                .map(DefaultMessageSourceResolvable::getDefaultMessage)
//                .collect(Collectors.joining("; "));
//        log.warn("参数校验失败: {}", errorMessage);
//        return Result.fail(400, errorMessage);
//    }
//
//    /**
//     * 处理业务异常
//     */
//    @ExceptionHandler(RuntimeException.class)
//    public Result<String> handleBusinessException(RuntimeException e) {
//        log.error("业务异常: {}", e.getMessage(), e);
//        return Result.fail(500, e.getMessage());
//    }
//
//    /**
//     * 处理系统异常
//     */
//    @ExceptionHandler(Exception.class)
//    public Result<String> handleException(Exception e) {
//        log.error("系统异常: {}", e.getMessage(), e);
//        return Result.fail(500, "系统繁忙，请稍后再试");
//    }
//}