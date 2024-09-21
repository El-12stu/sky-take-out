package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，用于表示某个方法需要进行功能字段自动填充处理
 */
@Target(ElementType.METHOD)//Only for method
@Retention(RetentionPolicy.RUNTIME)//注解用于指定一个注解的保留策略，即这个注解在何时可用
public @interface AutoFill {
    //数据库操作类型：update，insert
    OperationType value();

}
