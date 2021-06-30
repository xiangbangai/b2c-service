package com.huateng.config.aop;

import com.huateng.data.vo.json.Response;
import com.huateng.web.service.AopService;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


@Aspect
@Component
public class AopEntry {

    @Resource
    private AopService aopService;

    /**加签验签**/
    @Pointcut("execution(* com.huateng.web.controller.*.*(..))")
    public void doSomething() {}

    @Before("doSomething()")
    public void doBeforeValidateSignature () throws Exception {
        this.aopService.logAndSign();
    }

    @AfterReturning(pointcut = "doSomething()", returning = "response" )
    public void doAfterAddSign(Response response) throws Exception {
        this.aopService.responseAddInfo(response);
    }

}
