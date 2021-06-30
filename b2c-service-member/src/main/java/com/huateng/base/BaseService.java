package com.huateng.base;

import com.huateng.common.util.BusinessException;
import com.huateng.common.util.JacksonUtil;
import com.huateng.common.util.SnowflakeIdWorker;
import com.huateng.common.util.SysConstant;
import com.huateng.data.model.db2.ServiceErrorInfo;
import com.huateng.data.vo.ResInfo;
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

    @Resource
    private SnowflakeIdWorker snowflakeIdWorker;

    protected ServiceErrorInfo getErrorInfo(String errorCode) throws Exception{
        return globalService.getErrorInfo(errorCode);
    }

    protected ServiceErrorInfo getErrorForJson(String json) throws Exception {
        ServiceErrorInfo serviceErrorInfo = JacksonUtil.toObject(json, ServiceErrorInfo.class);
        return serviceErrorInfo;
    }

    protected String getSnowId() {
        return this.snowflakeIdWorker.nextId() + "";
    }


    /**
     * 判断remote返回
     * @param resInfo
     * @return
     * @throws Exception
     */
    protected String getResJson(ResInfo resInfo) throws Exception {
        if (!SysConstant.SYS_SUCCESS.equals(resInfo.getResCode())) {
            throw new BusinessException(getErrorForJson(resInfo.getResMsg()));
        }
        return resInfo.getData();
    }
}
