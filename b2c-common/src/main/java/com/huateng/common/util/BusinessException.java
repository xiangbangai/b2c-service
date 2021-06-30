package com.huateng.common.util;

import com.huateng.data.model.db2.ServiceErrorInfo;
import lombok.Data;

import java.text.MessageFormat;

/**
 * Created with b2c-common.
 * User: Sam
 * Date: 2018/7/20
 * Time: 14:20
 * Description:自定义业务异常
 */
@Data
public class BusinessException extends RuntimeException {

    private ServiceErrorInfo serviceErrorInfo;

    public BusinessException(ServiceErrorInfo serviceErrorInfo) {
        super(serviceErrorInfo.getErrorMsg());
        this.serviceErrorInfo = serviceErrorInfo;
    }

    public BusinessException(ServiceErrorInfo serviceErrorInfo, Object[] params){
        super(MessageFormat.format(serviceErrorInfo.getErrorMsg(), params));
        serviceErrorInfo.setErrorMsg(MessageFormat.format(serviceErrorInfo.getErrorMsg(), params));
        this.serviceErrorInfo = serviceErrorInfo;
    }

}
