package com.huateng.web.service;

import com.huateng.base.BaseService;
import com.huateng.common.util.JacksonUtil;
import com.huateng.config.log.LogStashDealUtils;
import com.huateng.data.vo.params.LogInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletionException;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/9/3
 * Time: 19:11
 * Description:
 */
@Slf4j
@Service
public class SendService extends BaseService {


    /**
     * @User Sam
     * @Date 2020/9/3
     * @Time 19:12
     * @Param 
     * @return 
     * @Description 发往日志系统
     */
    public void sendLogSystem(LogInfo logInfo){
        try {
//            logInfo.setToken(this.logService.getRedisToken());
//            LogResult logResult;
//            MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
//            Field[] field = logInfo.getClass().getDeclaredFields();
//            for (Field f : field) {
//                String methodName = f.getName().substring(0,1).toUpperCase() + f.getName().substring(1);
//                Method getMethod = logInfo.getClass().getMethod("get" + methodName);
//                multiValueMap.add(f.getName(), (String) getMethod.invoke(logInfo));
//            }
//            log.info("上送日志:{}", multiValueMap.toString());
//            logResult = JacksonUtil.toObject(RestTemplateUtil.httpPostUrl(url, multiValueMap), LogResult.class);
//            log.info("上送日志返回:{}", logResult.toString());
//            if (!logResult.isResult()) {
//                throw new BusinessException(getErrorInfo(SysConstant.E03000001),new Object[]{logResult.toString()});//日志上送异常
//            }

            LogStashDealUtils.log(JacksonUtil.toJson(logInfo));
        } catch (Exception e) {
            throw new CompletionException(e);
        }
    }

}
