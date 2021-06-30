package com.huateng.config.aop;

import com.huateng.web.service.AopDoSomethingService;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


@Aspect
@Component
public class AopEntry {

    @Resource
    private AopDoSomethingService aopDoSomethingService;

    /**加签验签**/
    @Pointcut("execution(* com.huateng.web.controller.*.*(..))")
    public void doSomething() {}

    @Before("doSomething()")
    public void doBeforeValidateSignature () throws Exception {
        this.aopDoSomethingService.validateToken();
    }

}
