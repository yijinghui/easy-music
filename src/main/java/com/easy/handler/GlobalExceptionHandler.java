package com.easy.handler;

import com.easy.constant.MessageConstant;
import com.easy.exception.BaseException;
import com.easy.result.Result;
import jakarta.validation.ConstraintDeclarationException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 处理SQL异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex){
        String message = ex.getMessage();
        if (message.contains("Duplicate entry")) {
            String username = message.split(" ")[2];
            String msg = username + "已存在";
            return Result.error(msg);
        }else {
            return Result.error("未知错误");
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleMethodArgumentNotValidException(MethodArgumentNotValidException ex){
        String message = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return Result.error(message);
    }




    @ExceptionHandler(org.springframework.web.method.annotation.HandlerMethodValidationException.class)
    public Result handleHandlerMethodValidationException(
            org.springframework.web.method.annotation.HandlerMethodValidationException ex) {

        // 从 getAllErrors 中提取错误信息
        String message = ex.getAllErrors().get(0).getDefaultMessage();

        // 如果提取不到，给个兜底提示
        if (message == null || message.isEmpty()) {
            message = "请求参数校验失败，请检查必填参数是否完整";
        }

        return Result.error(message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result handleConstraintViolationException(ConstraintViolationException ex){
        String message = ex.getConstraintViolations().iterator().next().getMessage();
        return Result.error(message);
    }
}
