package com.huateng.web.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.huateng.base.BaseService;
import com.huateng.common.util.*;
import com.huateng.config.apollo.ApolloBean;
import com.huateng.data.db2.mapper.ServiceInvoiceTxnMapper;
import com.huateng.data.db2.mapper.ServiceOrderDetailMapper;
import com.huateng.data.db2.mapper.ServiceOrderMapper;
import com.huateng.data.db3.mapper.EdclistnoMapper;
import com.huateng.data.db3.mapper.PayJMapper;
import com.huateng.data.db3.mapper.SaleJMapper;
import com.huateng.data.model.db2.*;
import com.huateng.data.model.db3.Edclistno;
import com.huateng.data.model.db3.PayJ;
import com.huateng.data.model.db3.SaleJ;
import com.huateng.data.vo.ResInfo;
import com.huateng.data.vo.params.*;
import com.huateng.service.remote.QueryCustRemote;
import com.huateng.service.remote.QueryInvoiceRemote;
import com.huateng.service.remote.QueryMemberPointsRemote;
import com.huateng.service.remote.QueryStationRemote;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.annotation.Resource;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/4/5
 * Time: 17:09
 * Description: 订单相关操作
 */
@Slf4j
@Service
public class MemberPointsRelateService extends BaseService {


    @Value("${WeChat.sendInvoice.url}")
    private String url;
    @Value("${WeChat.sendInvoice.username}")
    private String username;
    @Value("${WeChat.sendInvoice.password}")
    private String password;
    @Value("${WeChat.pointsChange.url}")
    private String pointsExchangeUrl;
    @Resource
    private ServiceOrderMapper serviceOrderMapper;
    @Resource
    private PayJMapper payJMapper;
    @Resource
    private SaleJMapper saleJMapper;
    @Resource
    private EdclistnoMapper edclistnoMapper;
    @Resource
    private ServiceOrderDetailMapper serviceOrderDetailMapper;
    @Resource
    private ServiceInvoiceTxnMapper serviceInvoiceTxnMapper;
    @Resource
    private QueryMemberPointsRemote queryMemberPointsRemote;
    @Resource
    private QueryStationRemote queryStationRemote;
    @Resource
    private QueryInvoiceRemote queryInvoiceRemote;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private ApolloBean apolloBean;
    @Resource
    private QueryCustRemote queryCustRemote;

    /**
     * 保存主订单
     * @param serviceOrder
     */
    @Transactional(rollbackFor = Exception.class)
    public ServiceOrder saveOrder(ServiceOrder serviceOrder) throws Exception {
        /**
         * 先拿锁，接着查询是否流水重复
         */
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(SysConstant.LOCK_SERVICE_ORDER);
        stringBuilder.append(serviceOrder.getChannel()).append(SysConstant.LOCK_SPLIT)
                .append(serviceOrder.getReqSerialNo()).append(SysConstant.LOCK_SPLIT)
                .append(serviceOrder.getOrderType());

        RLock rLock = this.redissonClient.getLock(stringBuilder.toString());

        try {
            boolean isOk = rLock.tryLock(this.apolloBean.getRedissonTimeWait(), this.apolloBean.getRedissonTimeExpired(), TimeUnit.SECONDS);
            //锁等待超时
            if (!isOk) {
                throw new BusinessException(getErrorInfo(SysConstant.SYS_LOCK_ERROR));//获取锁失败
            }
            OrderInfo orderInfo = new OrderInfo();
            orderInfo.setOrderType(serviceOrder.getOrderType());
            orderInfo.setChannel(serviceOrder.getChannel());
            orderInfo.setReqSerialNo(serviceOrder.getReqSerialNo());
            ServiceOrder order = getOrderInfo(orderInfo);
            orderInfo = null;
            /**
             * 有重复订单返回订单信息，没有则insert
             */
            if (order == null) {
                serviceOrderMapper.insert(serviceOrder);
                return null;
            } else {
                return order;
            }
        } catch (Exception e) {
            throw e;
        } finally {
            rLock.unlock();
        }
    }

    /**
     * 返回原定单信息
     * @param orderInfo
     * @return
     */
    public ServiceOrder getOrderInfo(OrderInfo orderInfo) throws Exception {
        ResInfo resInfo = this.queryMemberPointsRemote.queryOrderInfo(orderInfo);
        return JacksonUtil.toObject(getResJson(resInfo),  ServiceOrder.class);
    }

    /**
     * 保存发票流水
     * @param uploadInvoice
     */
    @Transactional(rollbackFor = Exception.class)
    public void uploadInvoice(UploadInvoice uploadInvoice) throws Exception {
        Edclistno edclistno = new Edclistno();
        ResInfo resInfo;
        resInfo = this.queryInvoiceRemote.queryPayJCount(uploadInvoice.getPay().get(0));
        Long n = Long.parseLong(getResJson(resInfo));

        if (n == 0) {
            for (PayJ p : uploadInvoice.getPay()) {
                this.payJMapper.insert(p);
            }
        }

        resInfo = this.queryInvoiceRemote.querySaleJCount(uploadInvoice.getSale().get(0));
        n = Long.parseLong(getResJson(resInfo));
            for (int i = 0; i < uploadInvoice.getSale().size(); i++) {
                SaleJ saleJ = uploadInvoice.getSale().get(i);
                if (i == 0) {
                    edclistno.setShopid(saleJ.getShopid());
                    edclistno.setDt(saleJ.getDt());
                    edclistno.setListno(saleJ.getListno());
                    edclistno.setPosId(saleJ.getPosId());
                    edclistno.setShiftid(saleJ.getShiftid());
                    edclistno.setTime(saleJ.getTime());
                    edclistno.setSublistno(saleJ.getSublistno());
                    edclistno.setWorkdate(saleJ.getWorkdate());
                }
                if (n == 0) {
                    this.saleJMapper.insert(saleJ);
                }
            }

        if (uploadInvoice.getEdcTaxType() != null && !"".equals(uploadInvoice.getEdcTaxType())) {
            edclistno.setTaxtype(uploadInvoice.getEdcTaxType());
            edclistno.setFlag(uploadInvoice.getEdcFlag());
            edclistno.setSeqno(uploadInvoice.getEdcSeqNo());
            resInfo = this.queryInvoiceRemote.queryEdcListNoCount(edclistno);
            n = Long.parseLong(getResJson(resInfo));
            if (n == 0) {
                this.edclistnoMapper.insert(edclistno);
            }
        }



    }


    /**
     * 更新商品信息
     * @param uploadInvoiceOrder
     * @param channel
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateGoods(UploadInvoiceOrder uploadInvoiceOrder, String channel) throws Exception {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setChannel(channel);
        orderInfo.setReqSerialNo(uploadInvoiceOrder.getReqSerialNo());
        orderInfo.setOrderType(SysConstant.ORDER_TYPE_EXCHANGE);
        ResInfo resInfo = this.queryMemberPointsRemote.queryOrderInfo(orderInfo);
        ServiceOrder serviceOrder = JacksonUtil.toObject(getResJson(resInfo),  ServiceOrder.class);
        if (serviceOrder != null) {
            for (ServiceOrderDetail serviceOrderDetail : uploadInvoiceOrder.getGoods()) {
                this.serviceOrderDetailMapper.updateGoods(serviceOrder.getId(), serviceOrderDetail);
            }
        } else {
            log.warn("找不到订单记录，渠道：{}，流水：{}", channel, uploadInvoiceOrder.getReqSerialNo());
        }
    }


    /**
     * 保存开票记录
     * @param serviceInvoiceTxn
     */
    public void saveInvoiceTxn(ServiceInvoiceTxn serviceInvoiceTxn) {
        WeChatResult weChatResult = new WeChatResult();
        try {
            weChatResult = sendInvoice(serviceInvoiceTxn.getSendInvoice());
            if (weChatResult.getResult().equals(200)) {
                serviceInvoiceTxn.setStatus(SysConstant.INVOICE_SEND_SUCCESS);
            } else {
                serviceInvoiceTxn.setStatus(SysConstant.INVOICE_SEND_FAIL);
            }
        } catch (Exception e) {
            log.error("调用开票服务失败：{}",e.getMessage());
            weChatResult.setMessage(SysConstant.SEND_WECHAT_ERROR); //官微接口调用异常
            serviceInvoiceTxn.setStatus(SysConstant.INVOICE_SEND_FAIL);
        }
        serviceInvoiceTxn.setCount(1);
        serviceInvoiceTxn.setResultMsg(weChatResult.getMessage());
        this.serviceInvoiceTxnMapper.insertSelective(serviceInvoiceTxn);
    }

    /**
     * 发送开票请求
     * @param sendInvoice
     */
    public WeChatResult sendInvoice(SendInvoice sendInvoice) throws Exception {
        String params = JacksonUtil.toJson(sendInvoice);
        String date = String.valueOf(DateUtil.getCurrentLocalDateTime().toInstant(DateUtil.getZoneOffset()).toEpochMilli());
        log.info("开票参数:{}", params);

        final StringBuilder builder = new StringBuilder(username);
        builder.append(":");
        builder.append(HttpRequestSiningHelper.createRequestSignature("POST", date, url, params, password));// 生成签名头信息

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(SysConstant.X_HMAC_AUTH_SIGNATURE, builder.toString());
        httpHeaders.add(SysConstant.X_HMAC_AUTH_METHOD, "HmacMD5");
        httpHeaders.add(SysConstant.X_HMAC_AUTH_DATE, date);

        WeChatResult weChatResult = JacksonUtil.toObject(RestTemplateUtil.httpPostForWeChat(url,params,httpHeaders), WeChatResult.class);
        log.info("收到微信响应:{}", weChatResult.toString());
        return weChatResult;
    }

    /**
     * 更新订单
     * @param serviceOrder
     */
    public int updateServiceOrder(ServiceOrder serviceOrder) {
        return this.serviceOrderMapper.updateByPrimaryKeySelective(serviceOrder);
    }


    /**
     * 推送微信消息
     * @param sendWeChat
     * @param type 0-积分兑换， 1-积分产生, 2-积分调整
     */
    public void sendWeChatMsg(SendWeChat sendWeChat, Integer type) throws Exception {
        if(null == sendWeChat || null == sendWeChat.getOpenId() || "".equals(sendWeChat.getOpenId())){
            log.info("OpenId为空,不满足推送条件");
            return;
        }
        ResInfo resInfo = this.queryStationRemote.queryStationInfo(sendWeChat.getCode());
        Station station = JacksonUtil.toObject(getResJson(resInfo),Station.class);
        if (station != null) {
            Map<String, ServiceDict> map = getServiceDict().get(SysConstant.DICT_KEY_1004000);
            //查询会员标签
            LabelParam labelParam = new LabelParam();
            labelParam.setCustId(sendWeChat.getCardNo());//会员id
            labelParam.setCurDate(new Date());//当前时间
            ResInfo custLabelInfo = this.queryCustRemote.queryCustLabel(labelParam);
            List<String> custLabelList = JacksonUtil.toObject(getResJson(custLabelInfo),new TypeReference<List<String>>(){});
            //查询会员标签组
            List<TblLabelGroup> labelGroupList = null;
            if(custLabelList != null && custLabelList.size() > 0){
                LabelGroupInfo labelGroupInfo = new LabelGroupInfo();
                labelGroupInfo.setLabelIds(custLabelList);
                ResInfo custLabelGroupInfo = this.queryCustRemote.custLabelGroup(labelGroupInfo);
                labelGroupList = JacksonUtil.toObject(getResJson(custLabelGroupInfo),new TypeReference<List<TblLabelGroup>>(){});
            }
            //根据标签组发送推送
            if(labelGroupList != null && labelGroupList.size() > 0){
                for (TblLabelGroup tblLabelGroup : labelGroupList) {
                    sendWeChat.setRemark(tblLabelGroup.getAdvContent());
                    sendWeChat.setLink(tblLabelGroup.getAdvUrl());
                    this.executeSendWeChatMsg(map,type,sendWeChat,station);
                }
            }else{
                this.executeSendWeChatMsg(map,type,sendWeChat,station);
            }


        }
    }


    /**
     * 执行推送
     */
    private void executeSendWeChatMsg(Map<String, ServiceDict> map,Integer type,SendWeChat sendWeChat,Station station) throws Exception{
        if (map != null) {
            switch (type) {
                case 0:
                    ServiceDict serviceDict = map.get(SysConstant.DICT_KEY_POINTS_CHANGE);
                    if (serviceDict != null) {
                        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                        params.add("title", SysConstant.MEMBER_POINTS_MSG_TITLE);
                        params.add("openId", sendWeChat.getOpenId());
                        params.add("cardNo", sendWeChat.getCardNo());
                        params.add("code",sendWeChat.getCode());
                        params.add("centent", MessageFormat.format(serviceDict.getDictValue(),
                                new Object[]{
                                        DateUtil.date2String(sendWeChat.getDate(), DateUtil.DATE_FORMAT_FULL),//消费时间
                                        station.getShortName().trim(),//油站中文名称
                                        SysConstant.MEMBER_POINTS_EXCHANGE_TYPE + sendWeChat.getIntegral().toString(),//行为+具体变动积分数
                                        NumberUtil.decimalFormat("#",sendWeChat.getTotalPoints())
                                }));
                        params.add("remark",sendWeChat.getRemark());
                        params.add("link",sendWeChat.getLink());
                        log.info("积分产生发送:{}", params.toString());
                        String result = RestTemplateUtil.httpPostUrl(pointsExchangeUrl, params);
                        log.info("[{}]积分产生微信返回：{}", sendWeChat.getOpenId(),result);
                    }
                    break;
                case 1:
                    ServiceDict serviceDictIncrease = map.get(SysConstant.DICT_KEY_POINTS_CHANGE);
                    if (serviceDictIncrease != null) {
                        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                        params.add("title", SysConstant.MEMBER_POINTS_MSG_TITLE);
                        params.add("openId", sendWeChat.getOpenId());
                        params.add("cardNo", sendWeChat.getCardNo());
                        params.add("code",sendWeChat.getCode());
                        params.add("centent", MessageFormat.format(serviceDictIncrease.getDictValue(),
                                new Object[]{
                                        DateUtil.date2String(sendWeChat.getDate(), DateUtil.DATE_FORMAT_FULL),//消费时间
                                        station.getShortName().trim(),//油站中文名称
                                        SysConstant.MEMBER_POINTS_PRODUCE_TYPE + sendWeChat.getIntegral().toString(),//行为+具体变动积分数
                                        NumberUtil.decimalFormat("#",sendWeChat.getTotalPoints())
                                }));
                        params.add("remark",sendWeChat.getRemark());
                        params.add("link",sendWeChat.getLink());
                        log.info("积分产生发送:{}", params.toString());
                        String result = RestTemplateUtil.httpPostUrl(pointsExchangeUrl, params);
                        log.info("[{}]积分产生微信返回：{}", sendWeChat.getOpenId(),result);
                    }
                    break;
                case 2:
                    ServiceDict serviceDictAdjust = map.get(SysConstant.DICT_KEY_POINTS_ADJUST);
                    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                    params.add("title", SysConstant.MEMBER_POINTS_MSG_TITLE);
                    params.add("openId", sendWeChat.getOpenId());
                    params.add("cardNo", sendWeChat.getCardNo());
                    params.add("code",sendWeChat.getCode());
                    params.add("centent", MessageFormat.format(serviceDictAdjust.getDictValue(),
                            new Object[]{
                                    sendWeChat.getCentent(),//原因
                                    sendWeChat.getAdjustFlag(),//操作
                                    NumberUtil.decimalFormat("#",sendWeChat.getIntegral())//调整的积分
                            }));
                    params.add("remark",sendWeChat.getRemark());
                    params.add("link",sendWeChat.getLink());
                    log.info("积分调整发送:{}", params.toString());
                    String result = RestTemplateUtil.httpPostUrl(pointsExchangeUrl, params);
                    log.info("[{}]积分调整微信返回：{}", sendWeChat.getOpenId(),result);
                    break;
                case 3:
                    MultiValueMap<String, String> externalAdjustParams = new LinkedMultiValueMap<>();
                    externalAdjustParams.add("title", sendWeChat.getTitle());
                    externalAdjustParams.add("openId", sendWeChat.getOpenId());
                    externalAdjustParams.add("cardNo", sendWeChat.getCardNo());
                    externalAdjustParams.add("code",sendWeChat.getCode());
                    externalAdjustParams.add("centent",sendWeChat.getCentent());
                    externalAdjustParams.add("remark",sendWeChat.getRemark());
                    externalAdjustParams.add("link",sendWeChat.getLink());
                    log.info("外部系统积分调整发送:{}", externalAdjustParams.toString());
                    String externalAdjust = RestTemplateUtil.httpPostUrl(pointsExchangeUrl, externalAdjustParams);
                    log.info("[{}]外部系统积分调整微信返回：{}", sendWeChat.getOpenId(),externalAdjust);
                    break;
            }
        }
    }

    /**
     * 新增商品信息
     * @param serviceOrderDetail
     * @param channel
     * @param reqSerialNo
     */
    @Transactional(rollbackFor = Exception.class)
    public void insertGoods(ServiceOrderDetail serviceOrderDetail, String channel, String reqSerialNo) throws Exception {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setChannel(channel);
        orderInfo.setReqSerialNo(reqSerialNo);
        orderInfo.setOrderType(SysConstant.ORDER_TYPE_PRODUCE);
        ResInfo resInfo = this.queryMemberPointsRemote.queryOrderInfo(orderInfo);
        ServiceOrder serviceOrder = JacksonUtil.toObject(getResJson(resInfo), ServiceOrder.class);
        if (serviceOrder != null) {
            this.serviceOrderDetailMapper.insert(serviceOrderDetail);
        } else {
            log.warn("找不到订单记录，渠道：{}，流水：{}", channel, reqSerialNo);
        }
    }

}
