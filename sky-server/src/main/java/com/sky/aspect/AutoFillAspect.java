package com.sky.aspect;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面类，实现公共字段自动填充处理逻辑
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    /**
     * 切入点表达式
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..))&& @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){

    }
    /**
     * 通知(前置）,在通知中进行公共字段的赋值
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("开始进行公共字段的自动填充");

        //1.获取当前被拦截的方法上的数据库操作类型
        MethodSignature methodsignature=(MethodSignature) joinPoint.getSignature();
        AutoFill autoFill=methodsignature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType=autoFill.value();
        //2.获取当前被拦截方法的参数--实体对象
       Object[] args=joinPoint.getArgs();
       if(args == null || args.length==0){
           return;
       }
       Object entity=args[0];
        //3.准备赋值的数据
        LocalDateTime now =LocalDateTime.now();
        Long currentId= BaseContext.getCurrentId();
        //4.根据不同的操作类型，为对应的属性通过反射赋值
        if(operationType == OperationType.INSERT){
            try {
                Method setCreateTime=entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME,LocalDateTime.class);
                Method setCreateUser=entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER,Long.class);
                Method setUpdateTime=entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);
                Method setUpdateUser=entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);

                //通过反射为对象属性赋值
                setCreateTime.invoke(entity,now);
                setCreateUser.invoke(entity,currentId);
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        else if (operationType == OperationType.UPDATE) {
            try {

                Method setUpdateTime=entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);
                Method setUpdateUser=entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);

                //通过反射为对象属性赋值

                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }


    }

}
