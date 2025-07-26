package com.daylily.global.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ExceptionHandlerLoggingAspect {

    /**
     * AOP를 사용하여 GlobalExceptionHandler의 예외 처리 메소드가 호출될 때마다 로그를 남깁니다.<br/>
     * handle로 시작하는 메소드에 대해서만 적용됩니다.
     */
    @Before("execution(* com.daylily.global.exception.GlobalExceptionHandler.handle*(..))")
    public void logExceptionHandler(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof Exception ex) {
            log.error(">>> Exception: {}", ex.getClass().getSimpleName());
            log.error(">>> Message: {}", ex.getMessage(), ex);
        }
    }
}
