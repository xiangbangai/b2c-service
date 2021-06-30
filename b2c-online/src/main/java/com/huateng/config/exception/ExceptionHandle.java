package com.huateng.config.exception;


import com.dianping.cat.Cat;
import com.huateng.base.BaseService;
import com.huateng.common.util.BusinessException;
import com.huateng.common.util.SysConstant;
import com.huateng.data.model.db2.ServiceErrorInfo;
import com.huateng.data.vo.json.Response;
import com.huateng.web.service.AopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestControllerAdvice
public class ExceptionHandle extends BaseService {

    @Resource
    private AopService aopService;
    @Resource
    private HttpServletRequest httpServletRequest;

    @ExceptionHandler({Exception.class})
    public Response handlerException(Exception e) {
        Response response = new Response();
        try {
            if(e instanceof BusinessException){
                ServiceErrorInfo serviceErrorInfo = ((BusinessException) e).getServiceErrorInfo();
                /**对外错误码不暴露内部错误**/
                if(serviceErrorInfo.getErrorCode().startsWith(SysConstant.SYS_ERROR_START_WITH)) {
                    response.setResCode(SysConstant.SYS_ERROR);
                    response.setResMsg(getErrorInfo(SysConstant.SYS_ERROR).getErrorMsg());
                } else {
                    response.setResCode(serviceErrorInfo.getErrorCode());
                    response.setResMsg(serviceErrorInfo.getErrorMsg());
                }
                /**部分异常需要上送Cat**/
                if(serviceErrorInfo.getErrorType() == 1){
                    Cat.logError(e);
                    Cat.logEvent(SysConstant.CAT_BUSINESS_ERROR,this.httpServletRequest.getRequestURI());
                }
                log.warn("业务异常：{}",serviceErrorInfo.getErrorMsg(), e);
            }else{
                Cat.logError(e);
                Cat.logEvent(SysConstant.CAT_SYSTEM_ERROR,this.httpServletRequest.getRequestURI());
                log.error("系统异常：{}", e.getMessage(),e);
                response.setResCode(SysConstant.SYS_ERROR);
                response.setResMsg(getErrorInfo(SysConstant.SYS_ERROR).getErrorMsg());
            }
            this.aopService.responseAddInfo(response);
        } catch (Exception ee) {
            Cat.logError(e);
            Cat.logEvent(SysConstant.CAT_SYSTEM_ERROR,this.httpServletRequest.getRequestURI());
            log.error("处理异常：{}", ee.getMessage(),ee);
        }
        return response;
    }
}
