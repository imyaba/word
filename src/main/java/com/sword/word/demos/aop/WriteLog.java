package com.sword.word.demos.aop;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


import java.time.LocalDateTime;

/**
 * @Author: sword
 * @Date: 2023/06/07/22:43
 * @Description:
 */
@Component
@Aspect
public class WriteLog {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * 进入方法时间戳
     */
    private Long startTime;
    /**
     * 方法结束时间戳(计时)
     */
    private Long endTime;
    //定义切点


    public WriteLog(){}

    /**
     *
     *   定义请求日志切入点，其切入点表达式有多种匹配方式,这里是指定路径
    @Pointcut("execution(public * cn.van.log.aop.controller.*.*(..))")
     */
    @Pointcut("@annotation(com.sword.word.demos.aop.WebLog)")
    public void aopWebLog() {}

    @Before("aopWebLog()")
    public void write(JoinPoint joinPoint )  {
        logger.info("请求开始前={} ---{}" , joinPoint.getSignature().getDeclaringTypeName() , joinPoint.getSignature().getName());

        // 获取注解
        MethodSignature signature =
                (MethodSignature)joinPoint.getSignature();
        WebLog annotation =signature.getMethod().getAnnotation(WebLog.class);
        logger.info("请求开始前请求参数={}",JSON.toJSONString(joinPoint.getArgs()));
        logger.info("请求开始前value={}",annotation.value());
        logger.info("请求开始前content={}",annotation.content());
        logger.info("请求开始前moduleName={}",annotation.moduleName());
        logger.info("请求开始前operateType={}",annotation.operateType());
        logger.info("请求开始前code={}",annotation.code());

    }

    @Around("aopWebLog()")
    public Object myLogger(ProceedingJoinPoint pjp) throws Throwable {
        startTime=System.currentTimeMillis();
        //使用ServletRequestAttributes请求上下文获取方法更多
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String className = pjp.getSignature().getDeclaringTypeName();
        String methodName = pjp.getSignature().getName();
        //使用数组来获取参数
        Object[] array = pjp.getArgs();
        ObjectMapper mapper = new ObjectMapper();
        //执行函数前打印日志
        logger.info("环绕调用前：{}：{},传递的参数为：{}", className, methodName, mapper.writeValueAsString(array));
        logger.info("环绕调用前URL:{}", request.getRequestURL().toString());
        logger.info("环绕调用前IP地址：{}", request.getRemoteAddr());
        //调用整个目标函数执行
        Object obj = pjp.proceed();
        //执行函数后打印日志
        logger.info("环绕调用后：{}：{},返回值为：{}", className, methodName, mapper.writeValueAsString(obj));
        logger.info("环绕耗时：{}ms", System.currentTimeMillis() - startTime);
        return obj;
    }
    /**
     * 返回通知：
     * 1. 在目标方法正常结束之后执行
     * 1. 在返回通知中补充请求日志信息，如返回时间，方法耗时，返回值，并且保存日志信息
     * w
     * @param ret
     * @throws Throwable
     */
    @AfterReturning(returning = "ret", pointcut = "aopWebLog()")
    public void doAfterReturning(Object ret) throws Throwable {
        endTime = System.currentTimeMillis();
        logger.info("请求结束时间：{}" , LocalDateTime.now());
        logger.info("请求耗时：{}" , (endTime - startTime));
        // 处理完请求，返回内容
        logger.info("请求返回 : {}" , JSON.toJSONString(ret));
    }

    /**
     * 异常通知：
     * 1. 在目标方法非正常结束，发生异常或者抛出异常时执行
     * 1. 在异常通知中设置异常信息，并将其保存
     *
     * @param throwable
     */
    @AfterThrowing(value = "aopWebLog()", throwing = "throwable")
    public void doAfterThrowing(Throwable throwable) {
        // 保存异常日志记录
        logger.error("发生异常时间：{}" , LocalDateTime.now());
        logger.error("抛出异常：{}" , throwable.getMessage());
    }

}
