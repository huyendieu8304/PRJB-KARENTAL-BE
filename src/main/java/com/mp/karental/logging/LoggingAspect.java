package com.mp.karental.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * This is an aspect for logging
 *
 * <p>
 * Log would be write
 * <ul>
 *     <li>before and after execution of methods in Controller, Service and Repository.</li>
 *     <li>after exception is threw in Controller, Service and Repository.</li>
 * </ul>
 * </p>
 *
 * @author DieuTTH4
 *
 * @version 1.0
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect {

//    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TenLop.class);


    /**
     * Log before method in Controller run
     */
    @Before("execution(* com.mp.karental.controller..*(..))")
    public void logBeforeController(JoinPoint joinPoint) {
        log.info("Calling controller: {}", joinPoint.getSignature());
    }

    /**
     * Log before method in Service run
     */
    @Before("execution(* com.mp.karental.service..*(..))")
    public void logBeforeService(JoinPoint joinPoint) {
        log.info("Calling service: {}", joinPoint.getSignature());
    }

    /**
     * Log before method in repository run
     */
    @Before("execution(* com.mp.karental.repository..*(..))")
    public void logBeforeRepository(JoinPoint joinPoint) {
        log.info("Calling repository: {}", joinPoint.getSignature());
    }

    /**
     * Log after finish method in Controller, Service, Repository
     */
    @After("execution(* com.mp.karental.controller..*(..)) || " +
            "execution(* com.mp.karental.service..*(..)) || " +
            "execution(* com.mp.karental.repository..*(..))")
    public void logAfterMethod(JoinPoint joinPoint) {
        log.info("Finish method: {}", joinPoint.getSignature());
    }


    /**
     * Log after exception is thew in Controller
     */
    @AfterThrowing(pointcut = "execution(* com.mp.karental.controller..*(..))", throwing = "exception")
    public void handleControllerException(Exception exception) {
        log.error("Exception in Controller: {}", exception.getMessage());
    }

    /**
     * Log after exception is thew in Service
     */
    @AfterThrowing(pointcut = "execution(* com.mp.karental.service..*(..))", throwing = "exception")
    public void handleServiceException(Exception exception) {
        log.error("Exception in Service: {}", exception.getMessage());
    }

    /**
     * Log after exception is thew in Repository
     */
    @AfterThrowing(pointcut = "execution(* com.mp.karental.repository..*(..))", throwing = "exception")
    public void handleRepositoryException(Exception exception) {
        log.error("Exception in Repository: {}", exception.getMessage());
    }

}
