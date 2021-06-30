package com.huateng.base;

import com.huateng.common.util.BusinessException;
import com.huateng.common.util.JacksonUtil;
import com.huateng.common.util.SnowflakeIdWorker;
import com.huateng.common.util.SysConstant;
import com.huateng.data.model.db2.ServiceChannel;
import com.huateng.data.model.db2.ServiceDict;
import com.huateng.data.model.db2.ServiceErrorInfo;
import com.huateng.data.model.db2.ServiceInterfaceInfo;
import com.huateng.data.vo.ResInfo;
import com.huateng.web.service.GlobalService;

import javax.annotation.Resource;
import java.util.Map;
import java.util.UUID;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2019/9/14
 * Time: 20:54
 * Description:
 */
public class BaseService {

    @Resource
    private GlobalService globalService;
    @Resource
    private SnowflakeIdWorker snowflakeIdWorker;

    protected ServiceErrorInfo getErrorInfo(String errorCode) throws Exception{
        return this.globalService.getErrorInfo(errorCode);
    }

    protected ServiceErrorInfo getErrorForJson(String json) throws Exception {
        ServiceErrorInfo serviceErrorInfo = JacksonUtil.toObject(json, ServiceErrorInfo.class);
        return serviceErrorInfo;
    }

    protected Map<String,Map<String,ServiceDict>> getServiceDict() throws Exception{
        return this.globalService.getDict();
    }

    protected Map<String, ServiceChannel> getChannel() throws Exception {
        return this.globalService.getChannel();
    }

    protected Map<String, ServiceInterfaceInfo> getServiceInfo() throws Exception {
        return this.globalService.getServiceInfo();
    }

    protected String getTxn() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    protected String getSnowId() {
        return String.valueOf(this.snowflakeIdWorker.nextId());
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
