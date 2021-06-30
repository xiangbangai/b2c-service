package com.huateng.base;

import com.huateng.data.model.db2.ServiceErrorInfo;
import com.huateng.web.service.GlobalService;

import javax.annotation.Resource;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2019/9/14
 * Time: 20:50
 * Description:
 */
public class BaseService {

    @Resource
    private GlobalService globalService;

    protected ServiceErrorInfo getErrorInfo(String errorCode) throws Exception{
        return globalService.getErrorInfo(errorCode);
    }
}
