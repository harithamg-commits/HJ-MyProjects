package com.example.books;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class BookControllerLoggingAspect {
    private static final Logger log = LoggerFactory.getLogger(BookControllerLoggingAspect.class);

    @Around("execution(public * com.example.books.BookController.*(..))")
    public Object logControllerCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().getName();
        log.info("BookController.{} called", method);
        try {
            Object result = joinPoint.proceed();
            log.info("BookController.{} completed", method);
            return result;
        } catch (Throwable error) {
            log.warn("BookController.{} failed: {}", method, error.toString());
            throw error;
        }
    }
}
