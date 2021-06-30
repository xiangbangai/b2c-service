package com.huateng.config.exception;


import com.dianping.cat.Cat;
import com.huateng.base.BaseService;
import com.huateng.common.util.BusinessException;
import com.huateng.common.util.JacksonUtil;
import com.huateng.common.util.SysConstant;
import com.huateng.data.model.db2.ServiceErrorInfo;
import com.huateng.data.vo.ResInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;


@Slf4j
@RestControllerAdvice
public class ExceptionHandle extends BaseService {

    @Resource
    private HttpServletRequest httpServletRequest;

    @ExceptionHandler({Exception.class})
    public ResInfo handlerException(Exception e) {
        ResInfo resInfo = new ResInfo();
        ServiceErrorInfo serviceErrorInfo;
        try {
            if(e instanceof BusinessException){
                serviceErrorInfo = ((BusinessException) e).getServiceErrorInfo();
                resInfo.setResCode(serviceErrorInfo.getErrorCode());
                resInfo.setResMsg(JacksonUtil.toJson(serviceErrorInfo));
                /**部分异常需要上送Cat**/
                if(serviceErrorInfo.getErrorType() == 1){
                    Cat.logError(e);
                    Cat.logEvent(SysConstant.CAT_BUSINESS_ERROR,this.httpServletRequest.getRequestURI());
                }
                log.warn("业务异常：{}",serviceErrorInfo.getErrorMsg(), e);
            }else{
                serviceErrorInfo = getErrorInfo(SysConstant.SYS_ERROR);
                Cat.logError(e);
                Cat.logEvent(SysConstant.CAT_SYSTEM_ERROR,this.httpServletRequest.getRequestURI());
                log.error("系统异常：{}", e.getMessage(),e);
                resInfo.setResCode(SysConstant.SYS_ERROR);
                resInfo.setResMsg(JacksonUtil.toJson(serviceErrorInfo));
            }
        } catch (Exception ee) {
            Cat.logError(e);
            Cat.logEvent(SysConstant.CAT_SYSTEM_ERROR,this.httpServletRequest.getRequestURI());
            log.error("处理异常：{}", ee.getMessage(),ee);
        }
        return resInfo;
    }
}
