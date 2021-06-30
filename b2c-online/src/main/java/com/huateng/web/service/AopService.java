package com.huateng.web.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.StrUtil;
import com.dianping.cat.Cat;
import com.huateng.base.BaseService;
import com.huateng.common.util.*;
import com.huateng.config.apollo.ApolloControl;
import com.huateng.data.model.db2.ServiceChannel;
import com.huateng.data.model.db2.ServiceInterfaceInfo;
import com.huateng.data.vo.json.Request;
import com.huateng.data.vo.json.Response;
import com.huateng.data.vo.params.LogInfo;
import com.huateng.service.remote.LogServiceRemote;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2019/9/16
 * Time: 19:40
 * Description:
 */
@Slf4j
@Service
public class AopService extends BaseService {

    @Resource
    private HttpServletRequest httpServletRequest;
    @Resource
    private HttpServletResponse httpServletResponse;
    @Value("${sign.isSign}")
    private boolean isSign;
    @Value("${sign.private}")
    private String privateKey;
    @Resource
    private ValidateService validateService;
    @Resource
    private ApolloControl apolloControl;
    @Resource
    private LogServiceRemote logServiceRemote;

    /**
     * @User Sam
     * @Date 2019/9/16
     * @Time 19:42
     * @Param 
     * @return 
     * @Description 日志记录与验签
     */
    public void logAndSign() throws Exception {
        String signature = this.httpServletRequest.getHeader(SysConstant.HTTP_HEAD_SIGNATURE); //渠道签名
        String proxySignature = this.httpServletRequest.getHeader(SysConstant.HTTP_HEAD_PROXY_SIGNATURE); //代理渠道签名
        String channel = this.httpServletRequest.getHeader(SysConstant.HTTP_HEAD_CHANNEL); //渠道
        String proxyChannel = this.httpServletRequest.getHeader(SysConstant.HTTP_HEAD_PROXY_CHANNEL); //代理渠道
        String signSrc = SecurityUtil.InputStreamToStr(this.httpServletRequest.getInputStream());
        String url = this.httpServletRequest.getRequestURI();

        Cat.logMetricForCount(url, 3);//1秒内调用3次一起上送
        Cat.logEvent(SysConstant.REQ_DATA, signSrc);

        log.info("收到请求：" + url);
        log.info("渠道：" + channel);
        log.info("请求报文：" + signSrc);

        //测试加签
        //String testPrivateKey="MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDu2mCy1JMEi6CPth309lTYDsrF3H5d9Rc8ZowVnXVqRSeoxEI0uqsmcIqYS1DFI7sQxD346ZnzvBFkN4+B0I2zH9nngcX/muFM4/IqThUdWvRXfcvbcleH3QWkUUG7aWEifssY1y3dkYdDchoiIpkqiGZ+SHdN117bFniRzYgMaROKwRcNrS08ZxT1OtGP7oeIhfreGi6BJi6SI6wZjnQUvHPMVH4E1v3FZIUB/TANs6uJU74XFCLIbhfKgD+jt0wGNvhoZ27ctTDYKew54iWHNzHi5byGdglFDxIOvBTiWNtFWMY3WKUMLVI5N+2TDATcD1oN/rTUi7LsmbVpd+nJAgMBAAECggEATnLxMas6AlMt0CW7nxpM8Y2iMexioM9/6zmvJuZJizbdeMPFDvaEgiKSksSh6a37od0ikZ+ADhqmB6lr+/IYqE18z1nDQ1mGSC/R+O0eaD7YY+gQTao3b4s7cCW0Fi7dVWwZnrYF/XDv4UoXpUCB2ANmznFUpf9ny0ONJC5CUfiKnksOTtS2SyFXPM84zvWCcV5V+SetRLj8Gw+GYh1BXbY+VX8A3GT0XTLR5FMWqeiRQ8CC/uWPoufvnQmoc4i2xSfYTwbM3y+OMx66iskijRdwM1FsmNp13UlBjUWq0C02Vi6MBFD31e78W98tkJiptnv13QW9p0bN5DTvOwE5AQKBgQD5vIAycxVdiaWYuFnt+5see9xoXmxAG1frLfX/XAC7Ni6Ts67KDbctgNrMC/5BQhQ+cTqUI/uGpReIRixIlBbuc1C08UubLUOgVu+VtBgFW7bQYsGzcwQ93LQPzv3kKei7XsOIvPAxr4E2O/b1O1SpdbCOdV1B870CSi5DZjQAIQKBgQD01/9zIfjz/HM5WVVT0yRC2aGr0Dxh4BmVp9rVbmKdnFNukUBtuDxJ4p6YCfn/TZmn6fGTF5DX4jdh6zx1WiKRbfO4lyvcLVz5DKbwNi5ABF+HTMRO+wb2Ns6l1Ty4cmksQ9CSnGYYDznrdHz0NN3Z+hZKYJtT31zk88hloflUqQKBgDcyX49G+mDrLGudy1qbMsTBEY0L2Zcts/fp96YSGqNGLXM90pMcBvGKulFBEVgxF7JZal4VueEfmhznjaAZAxx+5kan0lMg5QKKVHfdDzYX+EcEnNVhxX06y5123YzRanwHTOZrBBenN8LZ7BJ1o5e0yGokLJGIIq1f4evJqhKhAoGBAJj4DgfvdKty0MZGstGxO5lsgN5oNiFsMm/WtBjeY0xXK6ULqLYkAb6jkDQrH37LC7HlrmEYuwy1r6ZEorwquSglCr0L7YIc8VZD+lRfdVvXn5tXcdpqaToQmeDvrYULLfxSzg+bGQn1EVmyqJKmKOkTnzOO0dx/03jTeiNNJAopAoGBAO6Lok1vwC14JpKqMiwBaJlxqx5Hf35hI6w3RLrHzUqBokFDz+jhK4vYKmzJ2XqEeEfl88CFrrBfiYCa31v7DOhgb8wyqIobmzs2Y92FqS6wrbQMXjLBKOMh60ugSGmYXig2pHCDfjB0COVEfURfE/WhXjX2DhDa7BQpt6tfk5Rr";
        //String testPublicKey="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA7tpgstSTBIugj7Yd9PZU2A7Kxdx+XfUXPGaMFZ11akUnqMRCNLqrJnCKmEtQxSO7EMQ9+OmZ87wRZDePgdCNsx/Z54HF/5rhTOPyKk4VHVr0V33L23JXh90FpFFBu2lhIn7LGNct3ZGHQ3IaIiKZKohmfkh3Tdde2xZ4kc2IDGkTisEXDa0tPGcU9TrRj+6HiIX63hougSYukiOsGY50FLxzzFR+BNb9xWSFAf0wDbOriVO+FxQiyG4XyoA/o7dMBjb4aGdu3LUw2CnsOeIlhzcx4uW8hnYJRQ8SDrwU4ljbRVjGN1ilDC1SOTftkwwE3A9aDf601Iuy7Jm1aXfpyQIDAQAB";
        //signature = SecurityUtil.getSign(SecurityUtil.sha1X16(signSrc), testPrivateKey);
        //System.out.println("测试的签名："+signature);

        Request request = null;
        try {
            request = JacksonUtil.toObject(signSrc, Request.class);
        } catch (Exception e) {
            /**解析报文失败**/
            throw new BusinessException(getErrorInfo(SysConstant.E01000002));
        }
        this.httpServletRequest.setAttribute(SysConstant.JSON_OBJECT, request); //request对象后续使用

        /**请求流水判断**/
        this.validateService.validateForString(request.getReqSerialNo(), SysConstant.E01000012);
        /**请求时间格式判断**/
        this.validateService.validateForDateTime(request.getReqDateTime(), DateUtil.DATE_FORMAT_FULL, SysConstant.E01000014);

        /**检查渠道不为空，并且head中与报文体中一致**/
        if ("".equals(channel) || null == channel ||
                "".equals(request.getChannel()) || null == request.getChannel() ||
                !channel.equals(request.getChannel())) {
            /**渠道错误**/
            throw new BusinessException(getErrorInfo(SysConstant.E01000001));
        }

        /**打开验签开关后继续后续检查**/
        if(isSign) {
            Map<String, ServiceChannel> channelMap = getChannel();
            /**检查渠道属性**/
            if (!channelMap.containsKey(channel)) {
                throw new BusinessException(getErrorInfo(SysConstant.E01000001));
            }
            ServiceChannel firstChannel = channelMap.get(channel);
            ServiceChannel secondChannel = channelMap.get(proxyChannel);

            //外围渠道检查
            if( firstChannel.getChannelType() != 1) {
                throw new BusinessException(getErrorInfo(SysConstant.E01000001));
            }
            //需要代理渠道
            if (firstChannel.getMustProxy() == 1) {
                //代理渠道为空
                if(secondChannel == null) {
                    throw new BusinessException(getErrorInfo(SysConstant.E01000006));
                }
                //必须是代理渠道
                if(secondChannel.getChannelType() != 2) {
                    throw new BusinessException(getErrorInfo(SysConstant.E01000006));
                }
                //代理渠道关联错误
                if(!secondChannel.getId().equals(firstChannel.getParentId())) {
                    throw new BusinessException(getErrorInfo(SysConstant.E01000007));
                }

                log.info("代理渠道：" + proxyChannel);
                log.info("代理渠道签名：" + proxySignature);

                /**代理渠道验签**/
                if(!SecurityUtil.verifySign(signSrc, proxySignature,
                        SecurityUtil.getPublicKey(secondChannel.getCertInfo()))) {
                    throw new BusinessException(getErrorInfo(SysConstant.E01000008));
                }
            } else {
                log.info("渠道签名：" + signature);
                /**发起渠道验签**/
                if(!SecurityUtil.verifySign(signSrc, signature,
                        SecurityUtil.getPublicKey(firstChannel.getCertInfo()))) {
                    throw new BusinessException(getErrorInfo(SysConstant.E01000009));
                }
            }
        }
        this.httpServletRequest.setAttribute(SysConstant.JSON_STR, signSrc); //请求报文后续使用

    }


    /**
     * @User Sam
     * @Date 2019/9/16
     * @Time 20:31
     * @Param
     * @return
     * @Description 返回报文处理
     */
    public void responseAddInfo(Response response) throws Exception {
        Request request = (Request) this.httpServletRequest.getAttribute(SysConstant.JSON_OBJECT);
        response.setChannel(request.getChannel());
        response.setReqDateTime(request.getReqDateTime());
        response.setReqSerialNo(request.getReqSerialNo());
        if (response.getResSerialNo() == null || "".equals(response.getResSerialNo())) {
            response.setResSerialNo(getTxn());
        }
        if (response.getResDateTime() == null || "".equals(response.getResDateTime())) {
            response.setResDateTime(DateUtil.getCurrentDateFull());
        }
        String reponse = JacksonUtil.toJson(response).replaceAll("\\t|\\n","");
        log.info("返回报文：" + reponse);
        //单笔耗时 System.currentTimeMillis() - Cat.getManager().getPeekTransaction().getTimestamp()

        /**开启签名需要加签**/
        if(isSign) {
            String sign = SecurityUtil.getSign(SecurityUtil.sha1X16(reponse), privateKey);
            this.httpServletResponse.addHeader(SysConstant.HTTP_HEAD_SIGNATURE, sign);
            log.info("返回签名：" + sign);
        }




        /**
         * 上送日志
         */
        if (SysConstant.LOG_STATUS_ON.equals(this.apolloControl.getLogStatus())) {
            LogInfo logInfo = new LogInfo();
            logInfo.setChannel(response.getChannel());
            logInfo.setAccessDay(DateUtil.changeFormat4LocalDate(response.getReqDateTime(),DateUtil.DATE_FORMAT_FULL,DateUtil.DATE_FORMAT_SHORT));
            logInfo.setAccessTime(DateUtil.getMillis(DateUtil.string2LocalDateTime(response.getReqDateTime(),DateUtil.DATE_FORMAT_FULL)));
            logInfo.setReqSerialNo(response.getReqSerialNo());
            logInfo.setResSerialNo(response.getResSerialNo());
            logInfo.setResCode(response.getResCode());
            logInfo.setReqIp(Cat.getManager().getPeekTransaction().getChildren().get(0).getData().toString().
                    split(SysConstant.SPLIT_TYPE1)[0].split(SysConstant.SPLIT_TYPE2)[1]);
            logInfo.setUrl(this.httpServletRequest.getRequestURI());
            logInfo.setParams(this.httpServletRequest.getAttribute(SysConstant.JSON_STR).toString());
            logInfo.setActionName(this.getActionName(getServiceInfo(),this.httpServletRequest.getRequestURI()));
            logInfo.setResult(SysConstant.SYS_SUCCESS.equals(response.getResCode()) ? SysConstant.RESULT_SUCCESS : SysConstant.RESULT_FAIL);
            logInfo.setReturnValue(StringEscapeUtils.unescapeJavaScript(JacksonUtil.toJson(reponse)).replaceAll("^\"{1}|\"{1}$",""));
            logInfo.setResponseTime(String.valueOf(System.currentTimeMillis() - Cat.getManager().getPeekTransaction().getTimestamp()));

            if(request.getData() != null){
                Map<String,String> dataMap = Convert.convert(new TypeReference<LinkedHashMap<String,String>>(){}, request.getData());
                String custId = this.getMapValue(dataMap, "custId");
                String acctId = this.getMapValue(dataMap, "acctId");
                logInfo.setUserCode( !"".equals(custId) ? custId : acctId);
                logInfo.setBusinessDate(this.getMapValue(dataMap, "businessDate"));
                logInfo.setStationId(this.getMapValue(dataMap, "stationId"));
                logInfo.setPosId(this.getMapValue(dataMap, "posId"));
                logInfo.setShiftId(this.getMapValue(dataMap, "shiftId"));
                logInfo.setListNo(this.getMapValue(dataMap, "listNo"));
            }

            /**异步上送**/
            CompletableFuture.runAsync(() -> this.logServiceRemote.sendRemoteService(logInfo));
        }

    }


    /**
     * 获取接口名称
     * @param serviceInterfaceInfoMap
     * @param uri
     * @return
     */
    private String getActionName(Map<String, ServiceInterfaceInfo> serviceInterfaceInfoMap,String uri){
        if(CollUtil.isEmpty(serviceInterfaceInfoMap) || StrUtil.isBlank(uri)){
            return "";
        }
        ServiceInterfaceInfo serviceInterfaceInfo = serviceInterfaceInfoMap.get(uri);
        if(serviceInterfaceInfo == null){
            return "";
        }
        return serviceInterfaceInfo.getServiceName();
    }

    /**
     * 获取值
     * @param map
     * @param key
     * @return
     */
    private String getMapValue(Map<String,String> map,String key){
        String s = map.get(key);
        if(StrUtil.isBlank(s)){
            return "";
        }else{
            return s;
        }
    }


}
