package com.whurs.aspect;

import com.whurs.annotation.AutoFill;
import com.whurs.constant.AutoFillConstant;
import com.whurs.context.BaseContext;
import com.whurs.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自动填充字段切面类
 * 根据操作类型填充
 * Update:updateUser,updateTime
 * Insert:updateTime,updateUser,createUser,createTime
 */

@Aspect
@Slf4j
@Component
public class AutoFillAspect {
    //切面表达式：mapper下的所有类所有方法，且加上了AutoFull注解
    @Pointcut("execution(* com.whurs.mapper.*.*(..)) && @annotation(com.whurs.annotation.AutoFill)")
    public void autoFillPointCut(){}

    @Before("autoFillPointCut()")
    public void autoFillField(JoinPoint joinPoint){
        log.info("开始进行公共字段自动填充");
        //获取被拦截的方法的操作类型（insert还是update）
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType = autoFill.value();

        //获取当前被拦截方法的参数类型-对象
        Object[] args = joinPoint.getArgs();
        if(args==null||args.length==0){
            return;
        }
        Object entity = args[0];

        //准备赋值数据
        LocalDateTime now=LocalDateTime.now();
        Long id= BaseContext.getCurrentId();
        //根据不同的操作类型，通过反射为当前对象的公共字段赋值
        if(operationType==OperationType.INSERT){
            try {
                //获取方法
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                //为字段赋值
                setCreateTime.invoke(entity,now);
                setCreateUser.invoke(entity,id);
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,id);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if(operationType==OperationType.UPDATE){
            try {
                //获取方法
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                //为字段赋值
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,id);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }
}
