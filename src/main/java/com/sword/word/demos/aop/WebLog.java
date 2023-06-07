package com.sword.word.demos.aop;

import java.lang.annotation.*;

/**
 * @Author: sword
 * @Date: 2023/06/07/22:14
 * @Description:
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebLog {
     String value () default  "";
     //操作日志的内容
     String content() default "";
     //模块名
     String  moduleName() default "";
     //操作类型
     String operateType() default "";
     //操作编号
     String code() default "";
}
