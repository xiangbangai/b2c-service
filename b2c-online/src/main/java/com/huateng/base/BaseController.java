package com.huateng.base;

import com.huateng.common.util.BusinessException;
import com.huateng.common.util.JacksonUtil;
import com.huateng.common.util.SysConstant;
import com.huateng.data.model.db2.ServiceDict;
import com.huateng.data.vo.json.Request;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2019/9/14
 * Time: 20:54
 * Description:
 */
@RequestMapping("/online")
public class BaseController extends BaseService {

    @Resource
    private HttpServletRequest httpServletRequest;

    /**
     * @User Sam
     * @Date 2019/9/17
     * @Time 11:25
     * @Param 
     * @return 
     * @Description 获取json字符串
     */
    protected String getJson() {
        return (String) this.httpServletRequest.getAttribute(SysConstant.JSON_STR);
    }

    /**
     * @User Sam
     * @Date 2019/9/17
     * @Time 11:52
     * @Param 
     * @return
     * @Description 直接获取简单对象
     */
    protected <T> Request getObject(Class<T> c) throws Exception {
        try {
            Request request = JacksonUtil.toObject(getJson(), JacksonUtil.getObjectType(Request.class, c));
            if (request.getData() == null) {
                throw new BusinessException(getErrorInfo(SysConstant.SYS_PARAMS_NULL)); //请求参数为空
            }
            return request;
        } catch (Exception e) {
            throw new BusinessException(getErrorInfo(SysConstant.E01000002));
        }
    }

    /**
     * 校验渠道手机
     * @param acctId 会员唯一属性
     * @param channel 渠道编号
     * @throws Exception
     */
    protected void validateChannelForMobile(String acctId, String channel) throws Exception {
        if (acctId.length() == 11 && !acctId.startsWith(SysConstant.START_WITH_MEMBER)) {
            Map<String, ServiceDict> map = getServiceDict().get(SysConstant.DICT_KEY_1001000);
            if(map != null && map.containsKey(channel)) {
                throw new BusinessException(getErrorInfo(SysConstant.E01000005));
            }
        }
    }

    /**
     * 校验手机号码
     * @param acctId
     * @param errorCode
     * @throws Exception
     */
    protected void validateForMobile(String acctId, String errorCode) throws Exception {
        if (acctId.length() == 11 && !acctId.startsWith(SysConstant.START_WITH_MEMBER)) {
            throw new BusinessException(getErrorInfo(errorCode));
        }
    }

    /**
     * 校验渠道相关参数
     * @param data 数据
     * @param target 目标数据
     * @param channel 渠道
     * @param errorCode 错误码
     * @throws Exception
     */
    protected void validateChannelForParams(String data, String target, String channel, String errorCode) throws Exception {
        Map<String, ServiceDict> map = getServiceDict().get(SysConstant.DICT_KEY_1001000);
        if(map != null && map.containsKey(channel)) {
            if (data.equals(target)) {
                throw new BusinessException(getErrorInfo(errorCode));
            }
        }
    }

}
