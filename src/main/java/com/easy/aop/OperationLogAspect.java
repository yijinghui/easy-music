package com.easy.aop;


import com.easy.mapper.OperationLogMapper;
import com.easy.pojo.entity.OperationLog;
import com.easy.utils.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class OperationLogAspect {

    @Autowired
    OperationLogMapper operationLogMapper;


    @Around("@annotation(com.easy.annotation.LogOperation)")
    public Object logOperation(ProceedingJoinPoint joinPoint) throws Throwable {

        // 获取方法执行开始时间
        long startTime = System.currentTimeMillis();

        // 获取用户id
        Long operatorId = ThreadLocalUtil.getUserId();

        // 获取签名
        Signature signature = joinPoint.getSignature();

        // 执行目标放法
        Object result = joinPoint.proceed();


        // 计算耗时
        long endTime = System.currentTimeMillis();
        long costTime = endTime-startTime;

        // 构建日志实体
        OperationLog operationLog=new OperationLog();
        operationLog.setOperatorTime(LocalDateTime.now());
        operationLog.setOperatorId(operatorId);
        operationLog.setClassName(joinPoint.getTarget().getClass().getName());
        operationLog.setCostTime(costTime);
        operationLog.setMethodName(signature.getName());
        operationLog.setMethodParams(Arrays.toString(joinPoint.getArgs()));
        operationLog.setReturnValue(result!=null?result.toString():"void");

        log.info("记录操作日志：{}",operationLog);
        operationLogMapper.insert(operationLog);

        return result;

    }
}
