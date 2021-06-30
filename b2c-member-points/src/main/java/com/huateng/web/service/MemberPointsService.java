package com.huateng.web.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.huateng.base.BaseService;
import com.huateng.common.util.BusinessException;
import com.huateng.common.util.DateUtil;
import com.huateng.common.util.JacksonUtil;
import com.huateng.common.util.SysConstant;
import com.huateng.config.apollo.ApolloBean;
import com.huateng.config.apollo.ApolloControl;
import com.huateng.config.apollo.ApolloYML;
import com.huateng.data.db2.mapper.*;
import com.huateng.data.model.db2.*;
import com.huateng.data.vo.ResInfo;
import com.huateng.data.vo.params.*;
import com.huateng.service.remote.QueryCustRemote;
import com.huateng.service.remote.QueryLimitRemote;
import com.huateng.service.remote.QueryMemberPointsRemote;
import com.huateng.service.remote.QueryStationRemote;
import com.huateng.toprules.adapter.*;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MemberPointsService extends BaseService {

    @Value("${rule.name}")
    private String ruleFileName;
    @Value("${WeChat.pointsChange.url}")
    private String pointsExchangeUrl;
    @Resource
    private RulePkgWorker rulePkgWorker;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private ApolloControl apolloControl;
    @Resource
    private ApolloBean apolloBean;
    @Resource
    private ApolloYML apolloYML;
    @Resource
    private QueryLimitRemote queryLimitRemote;
    @Resource
    private QueryCustRemote queryCustRemote;
    @Resource
    private QueryMemberPointsRemote queryMemberPointsRemote;
    @Resource
    private QueryStationRemote queryStationRemote;

    @Resource
    private TblBonusPlanMapper tblBonusPlanMapper;
    @Resource
    private TblBonusPlanDetailMapper tblBonusPlanDetailMapper;
    @Resource
    private TblBonusDelayMapper tblBonusDelayMapper;
    @Resource
    private TblFuelConsumptionMapper tblFuelConsumptionMapper;
    @Resource
    private TblBonusDetailAccountMapper tblBonusDetailAccountMapper;

    @Resource
    private ServiceOrderMapper serviceOrderMapper;
    @Resource
    private ServiceOrderDetailMapper serviceOrderDetailMapper;
    @Resource
    private ServiceBonusDetailMapper serviceBonusDetailMapper;
    @Resource
    private ServiceCustLimitMapper serviceCustLimitMapper;

    @Resource
    private TblOrderMapper tblOrderMapper;
    @Resource
    private TblOrderDetailMapper tblOrderDetailMapper;
    @Resource
    private TblBonusDetailMapper tblBonusDetailMapper;
    @Resource
    private TblTxnDetailMapper tblTxnDetailMapper;
    @Resource
    private TblCheckInfMapper tblCheckInfMapper;

    @Resource
    private MemberPointsRelateService memberPointsRelateService;

    @Resource
    private ValidateService validateService;

    /**
     * 积分兑换操作
     * @param id
     * @param custId
     * @param exchangeGoods
     * @param totalPrice
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public ExchangeResult exchange(String id, String custId, Date date, List<ExchangeGoods> exchangeGoods, BigDecimal totalPrice) throws Exception{
        /** 结果对象 **/
        ExchangeResult result = new ExchangeResult();
        result.setTotalConsume(totalPrice);
        BigDecimal expBonus = BigDecimal.ZERO;//用于返回本年将到期积分
        String expDate = DateUtil.date2String(date, DateUtil.DATE_YEAR) + SysConstant.YEAR_FLAG;//今年年底


        /**
         * 操作之前先获取锁
         * 单位时间内只允许一个线程操作一个用户的积分账户exchange
         */
        String lockName = SysConstant.LOCK_MEMBER_POINTS + custId;
        RLock rLock = this.redissonClient.getLock(lockName);
        try {
            boolean isOk = rLock.tryLock(this.apolloBean.getRedissonTimeWait(), this.apolloBean.getRedissonTimeExpired(), TimeUnit.SECONDS);
            //锁等待超时
            if (!isOk) {
                throw new BusinessException(getErrorInfo(SysConstant.SYS_LOCK_ERROR));//获取锁失败
            }

            //积分账户
            ResInfo bonusPlanResInfo = queryMemberPointsRemote.queryBonusPlanByCustId(custId, SysConstant.BP_PLAN_TYPE_DEFAULT);
            TblBonusPlan tblBonusPlan = JacksonUtil.toObject(getResJson(bonusPlanResInfo), TblBonusPlan.class);

            if (tblBonusPlan == null || tblBonusPlan.getValidBonus().compareTo(totalPrice) == -1) {
                throw new BusinessException(getErrorInfo(SysConstant.E02000004)); //积分余额不足
            }
            if (SysConstant.BP_LOCK_STATUS_LOCKED.equals(tblBonusPlan.getLockStatus())) {
                throw new BusinessException(getErrorInfo(SysConstant.E02000005)); //客户已经被冻结
            }

            BigDecimal validBonus = NumberUtil.null2Zero(tblBonusPlan.getValidBonus());//账户可用积分
            BigDecimal applyBonus = NumberUtil.null2Zero(tblBonusPlan.getApplyBonus());//账户已用积分
            tblBonusPlan.setValidBonus(validBonus.subtract(totalPrice));
            tblBonusPlan.setApplyBonus(applyBonus.add(totalPrice));
            tblBonusPlan.setModifyOper(custId);
            tblBonusPlan.setModifyDate(DateUtil.date2String(date, DateUtil.DATE_FORMAT_COMPACT));
            tblBonusPlan.setModifyTime(DateUtil.date2String(date, DateUtil.DATE_FORMAT_TIME));
            this.tblBonusPlanMapper.updatePlanBonus(tblBonusPlan);

            /**更新订单**/
            ServiceOrder serviceOrder = new ServiceOrder();
            serviceOrder.setId(id);
            serviceOrder.setNumber(totalPrice);
            serviceOrder.setReturnableNumber(totalPrice);
            serviceOrder.setValidBefore(validBonus);
            serviceOrder.setValidAfter(validBonus.subtract(totalPrice));
            serviceOrder.setStatus(SysConstant.ORDER_STATUS_SUCCESS);
            serviceOrderMapper.updateByPrimaryKeySelective(serviceOrder);

            /**记录商品**/
            ServiceOrderDetail serviceOrderDetail;
            for (ExchangeGoods e : exchangeGoods) {
                serviceOrderDetail = new ServiceOrderDetail();
                serviceOrderDetail.setOrderId(id);
                serviceOrderDetail.setId(e.getId());
                serviceOrderDetail.setGoodsId(e.getGoodsId());
                serviceOrderDetail.setGoodsName(e.getGoodsName());
                serviceOrderDetail.setGoodsType(SysConstant.GOODS_TYPE_NOT_OIL);
                serviceOrderDetail.setDiscountType(e.getDiscountType());
                serviceOrderDetail.setNumber(e.getNumber());
                serviceOrderDetail.setUnitPrice(e.getUnitPrice());
                serviceOrderDetail.setTotalPrice(e.getTotalPrice());
                serviceOrderDetail.setReturnableNumber(e.getNumber());
                serviceOrderDetailMapper.insert(serviceOrderDetail);
            }

            //积分明细
            BonusPlanDetailParams bonusPlanDetailParams = new BonusPlanDetailParams();
            bonusPlanDetailParams.setCustId(custId);
            bonusPlanDetailParams.setBpPlanType(SysConstant.BP_PLAN_TYPE_DEFAULT);
            bonusPlanDetailParams.setValidBonus(BigDecimal.ZERO);
            bonusPlanDetailParams.setExpiredStatus(SysConstant.BONUS_PLAN_DETAIL_IN_EFFECT);
            ResInfo bonusPlanDetailResInfo = queryMemberPointsRemote.queryBonusPlanDetail(bonusPlanDetailParams);
            bonusPlanDetailParams = null;
            List<TblBonusPlanDetail> bonusPlanDetailList = JacksonUtil.toObject(getResJson(bonusPlanDetailResInfo), new TypeReference<List<TblBonusPlanDetail>>() {});


            if (bonusPlanDetailList != null) {
                BigDecimal resultTemp;
                BigDecimal thisTemp;
                int orderSerial = 0;
                ServiceBonusDetail serviceBonusDetail;

                for (TblBonusPlanDetail tblBonusPlanDetail : bonusPlanDetailList) {
                    if (totalPrice.compareTo(BigDecimal.ZERO) == 1) {//需要扣积分
                        resultTemp = tblBonusPlanDetail.getValidBonus().subtract(totalPrice);

                        if (resultTemp.compareTo(BigDecimal.ZERO) == -1) { //没扣完，继续扣
                            thisTemp = totalPrice.subtract(resultTemp.abs());//本次扣了多少
                            totalPrice = resultTemp.abs(); //剩余待扣
                            tblBonusPlanDetail.setValidBonus(BigDecimal.ZERO); //原有全部扣完
                        } else {
                            thisTemp = totalPrice; //本次扣了多少
                            totalPrice = BigDecimal.ZERO; //剩余待扣
                            tblBonusPlanDetail.setValidBonus(resultTemp); //原有剩余多少
                        }


                        tblBonusPlanDetail.setApplyBonus(tblBonusPlanDetail.getApplyBonus().add(thisTemp));
                        tblBonusPlanDetail.setModifyOper(custId);
                        tblBonusPlanDetail.setModifyDate(DateUtil.date2String(date, DateUtil.DATE_FORMAT_COMPACT));
                        tblBonusPlanDetail.setModifyTime(DateUtil.date2String(date, DateUtil.DATE_FORMAT_TIME));
                        this.tblBonusPlanDetailMapper.updatePlanDetailBonus(tblBonusPlanDetail);

                        serviceBonusDetail = new ServiceBonusDetail();
                        serviceBonusDetail.setOrderId(id);//订单编号
                        serviceBonusDetail.setOrderSerial(orderSerial);//序号
                        serviceBonusDetail.setOperate(SysConstant.BONUS_OPERATE_DECREASE);//积分减少操作
                        serviceBonusDetail.setValidDate(tblBonusPlanDetail.getValidDate());//积分有效期
                        serviceBonusDetail.setNumber(thisTemp);//交易积分
                        serviceBonusDetail.setRuleId("");//规则编号
                        serviceBonusDetail.setReturnableNumber(thisTemp); //可退

                        serviceBonusDetailMapper.insert(serviceBonusDetail);

                        orderSerial++;
                    }
                    if (expDate.equals(tblBonusPlanDetail.getValidDate())) {
                        expBonus = tblBonusPlanDetail.getValidBonus();
                    }
                }
            }

            /**待扣积分未扣完**/
            if (totalPrice.compareTo(BigDecimal.ZERO) == 1) {
                throw new BusinessException(getErrorInfo(SysConstant.E02000002));//积分账户与明细不符
            }

            result.setTotalBonus(tblBonusPlan.getValidBonus());//当前总有效积分
            result.setExpBonus(NumberUtil.null2Zero(expBonus));//最近将到期积分
            result.setExpDate(expDate);//最近将到期的有效期
        } catch (Exception e) {
            throw e;
        } finally {
            rLock.unlock();
        }
        return result;
    }

    /**
     * 积分计算
     * @return
     * @throws Exception
     */
    public List<BonusAccItf> excuteComputeBonus(ComputeBonus computeBonus)throws Exception{
        List<BonusAccItf> resultList = new ArrayList<>(); //计算结果

        String channel = computeBonus.getChannel();

        //主机日期 主机时间 营业日期
        String txnyyyyMMdd = DateUtil.date2String(computeBonus.getReqDateTime(), DateUtil.DATE_FORMAT_COMPACT);
        String txnHHmmss = DateUtil.date2String(computeBonus.getReqDateTime(), DateUtil.DATE_FORMAT_TIME);
        String businessDate = DateUtil.date2String(computeBonus.getBusinessDate(), DateUtil.DATE_FORMAT_COMPACT);

        //自定义的卡bin
        Object cardBinObj = getRuleInfo().get(SysConstant.DICT_KEY_RULE_CARD_BIN);
        List<String> diyCadBinValue = cardBinObj == null ? null : (List<String>)cardBinObj;

        //自定义卡bin截取范围
        Map<String, ServiceDict> diyCardBinRange = getServiceDict().get(SysConstant.DICT_KEY_1005000);
        ServiceDict serviceDict = diyCardBinRange.get(SysConstant.DICT_KEY_RULE_CARD_BIN_RANGE);
        String[] diyCardBin = serviceDict.getDictValue().split(",");
        int cardBinStart = Integer.parseInt(diyCardBin[0]);//自定义卡bin开始索引
        int cardBinEnd = Integer.parseInt(diyCardBin[1]);//自定义卡bin结束索引

        //等级系数
        Map<String, Double> levelCoefficient = apolloYML.getUserLevel();
        //指定不能积分的商品列表
        Map<Integer, ServiceNotProduceMidtype> notProduceMidtypeMap = this.getNotProduceMidtypeMap();
        List<Goods> goods = this.analysisNoPointsList(computeBonus.getGoods(), notProduceMidtypeMap);
        computeBonus.setGoods(goods);

        //油站信息
        ResInfo stationResInfo = queryStationRemote.queryStationInfo(computeBonus.getStationId());
        Station station = JacksonUtil.toObject(getResJson(stationResInfo),Station.class);

        //支付方式 支付银联卡号
        Set<String> payTypeSet = new HashSet<>();
        String unionPayCardNo = "";
        for (PayInfo payInfo : computeBonus.getPayment()) {
            String payType = payInfo.getPayType();
            String subType = payInfo.getSubType();
            if(SysConstant.PAY_TYPE_UNIONPAY .equals(payType)){
                unionPayCardNo = payInfo.getPayInfo();
            }
            if(StrUtil.isNotBlank(subType)){
                payType = payType+subType;
            }
            payTypeSet.add(payType);
        }

        //银联卡是否存在白名单中
        boolean isWhite = false;
        if(StrUtil.isNotBlank(unionPayCardNo)){
            ResInfo whiteListResInfo = this.queryLimitRemote.queryCustUnionPayWhiteList(unionPayCardNo);
            if(Integer.parseInt(whiteListResInfo.getData()) > 0){
                isWhite = true;
            }
        }

        //会员标签
        LabelParam labelParam = new LabelParam();
        labelParam.setCustId(computeBonus.getCustInf().getCustId());//会员id
        labelParam.setCurDate(computeBonus.getReqDateTime());//当前时间
        ResInfo resInfo = this.queryCustRemote.queryCustLabel(labelParam);
        List<String> custLabelList = JacksonUtil.toObject(getResJson(resInfo),new TypeReference<List<String>>(){});
        List<String> labelList1 = dealLabelRepat(custLabelList);
        custLabelList = null;
        labelParam = null;

        //会员生日
        String birthday = "";
        ResInfo custUserResInfo = this.queryCustRemote.queryCustUser(computeBonus.getCustInf().getCustId());
        TblCustUser tblCustUser = JacksonUtil.toObject(getResJson(custUserResInfo),TblCustUser.class);
        if(tblCustUser != null){
            if(StrUtil.isNotBlank(tblCustUser.getBrithDate()) && (tblCustUser.getBrithDate().length() == 8)){
                birthday = tblCustUser.getBrithDate();
            }
        }


        List<TxnBonus> txnBonusList = new ArrayList<>();
        if(CollUtil.isEmpty(labelList1)){//会员没有标签
            this.genTxnBonusList(channel,txnBonusList,txnyyyyMMdd,txnHHmmss,businessDate,diyCadBinValue,cardBinStart,cardBinEnd,levelCoefficient,station,payTypeSet,unionPayCardNo,isWhite,computeBonus,birthday,"");
        }else{//会员有标签
            for (String label : labelList1) {
                this.genTxnBonusList(channel,txnBonusList,txnyyyyMMdd,txnHHmmss,businessDate,diyCadBinValue,cardBinStart,cardBinEnd,levelCoefficient,station,payTypeSet,unionPayCardNo,isWhite,computeBonus,birthday,label);
            }
        }

        if(txnBonusList != null){
            TopRules topRules = rulePkgWorker.getRuleFile();
            topRules.execPkg(txnBonusList, ruleFileName);
            //取出计算结果
            List<BonusAccItf> bonusAccItfList = getResultFromTxnBonus(txnBonusList);
            Map<String,String> checkMap = new HashMap<>();
            for (BonusAccItf bonusAccItf : bonusAccItfList) {
                if (checkMap.get(bonusAccItf.getRuleId() + bonusAccItf.getGoodsId()) == null) {
                    resultList.add(bonusAccItf);
                    checkMap.put(bonusAccItf.getRuleId() + bonusAccItf.getGoodsId(), bonusAccItf.getRuleId());
                }
            }
        }

        //没有产生积分，记录积分参数
        if(resultList.size() == 0){
            log.info("没有产生积分的参数");
            for (TxnBonus txnBonus : txnBonusList) {
                log.info("请求流水[{}],积分参数[{}]",computeBonus.getReqSerialNo(),txnBonus);
            }
        }

        return resultList;
    }

    //剔除不能积分的商品中类
    private List<Goods> analysisNoPointsList(List<Goods> goodsList, Map<Integer, ServiceNotProduceMidtype> noPointsList) {
        List<Goods> goods = new ArrayList<>();
        if(CollUtil.isEmpty(noPointsList)){
            return goodsList;
        }

        for (Goods good : goodsList) {
            if(noPointsList.get(good.getMiddleType()) == null){
                goods.add(good);
            }
        }

        return goods;
    }

    /**
     * 处理标签重复问题
     * @param labelList
     * @return
     */
    private List<String> dealLabelRepat(List<String> labelList) {
        if(CollUtil.isEmpty(labelList)) {
            return labelList;
        }
        boolean bonusSpeed = false;//判断是否有油品加速等级标签
        boolean crm = false;//判断是否有CRM加速标签
        boolean bjkhy = false;//判断是否为白金卡会员
        boolean zkhy = false;//判断是否为钻卡会员
        int bonusSpeedIndex = 0;//索引位置，根据索引删除
        int bjkhyIndex = 0;//索引位置，根据索引删除
        for(int i = 0; i<labelList.size(); i++) {
            String labelId = labelList.get(i);
            if(labelId.startsWith("20200401label1")) {
                bonusSpeed = true;
                bonusSpeedIndex = i;
            }
            if(labelId.startsWith("CRM_2020")) {
                crm = true;
            }
            if(labelId.equals("BJKHY")) {
                bjkhy = true;
                bjkhyIndex = i;
            }
            if(labelId.equals("ZKHY")) {
                zkhy = true;
            }
        }

        if(bonusSpeed && crm) {
            labelList.remove(bonusSpeedIndex);
        }
        if(bjkhy && zkhy) {
            labelList.remove(bjkhyIndex);
        }
        return labelList;
    }

    /**
     * 封装计算积分参数
     * @param txnBonusList
     * @param txnyyyyMMdd
     * @param txnHHmmss
     * @param businessDate
     * @param diyCadBinValue
     * @param cardBinStart
     * @param cardBinEnd
     * @param levelCoefficient
     * @param station
     * @param payTypeSet
     * @param unionPayCardNo
     * @param isWhite
     * @param computeBonus
     * @param birthday
     * @param label
     * @return
     * @throws Exception
     */
    private List<TxnBonus> genTxnBonusList(String channel,List<TxnBonus> txnBonusList,String txnyyyyMMdd, String txnHHmmss, String businessDate, List<String> diyCadBinValue,int cardBinStart, int cardBinEnd, Map<String, Double> levelCoefficient, Station station, Set<String> payTypeSet, String unionPayCardNo,boolean isWhite, ComputeBonus computeBonus, String birthday,String label) throws Exception{
        Map<String, TxnBonus> tempTxnBonusMap = new HashMap<>();//业务要求拼单处理，将所有礼品号相同的商品数量和支付价格合并

        //油站信息
        String stationShortName = "";
        String region = "";
        if(station != null){
            stationShortName = station.getShortName();
            region = station.getMarketRegionId().toString();
        }
        //客户信息
        TblCustInf custInf = computeBonus.getCustInf();

        for (Goods goods : computeBonus.getGoods()) {
            Short goodsType = goods.getGoodsType();//商品类型（0-非油品 1-油品 2-手工录入油品）
            String goodsId = goods.getGoodsId();//商品编号
            String goodsName = goods.getGoodsName();//商品名称
            String middleType = String.valueOf(goods.getMiddleType());//商品中类
            String litType = String.valueOf(goods.getLitType());//商品小类
            double totalPrice = goods.getTotalPrice().doubleValue();//单项商品总价
            double unitPrice = goods.getUnitPrice().doubleValue();//商品单价
            double goodsNumber = goods.getNumber().doubleValue();//商品购买数量

            TxnBonus txnBonus = null;
            if(SysConstant.GOODS_TYPE_NOT_OIL.equals(goodsType)){ //非油品
                txnBonus = tempTxnBonusMap.get(SysConstant.NOT_OIL_FLAG);
            }else{ //油品
                txnBonus = tempTxnBonusMap.get(middleType);
            }

            if(txnBonus != null){
                txnBonus.setTxn_amt(txnBonus.getTxn_amt()+ totalPrice);
                txnBonus.setGoods_num(txnBonus.getGoods_num() + goodsNumber);
                txnBonus.setGoods_count(txnBonus.getGoods_count() + goodsNumber);
                txnBonus.setGoods_act_price(txnBonus.getGoods_act_price()+ totalPrice);
            }else{
                txnBonus = new TxnBonus();
                txnBonus.setTxn_date(txnyyyyMMdd);//交易日期
                txnBonus.setTxn_time(txnHHmmss);// 交易时间
                txnBonus.setCust_id(custInf.getCustId());//客户号
                txnBonus.setAcct_id(computeBonus.getAcctId());//账号
                txnBonus.setAcct_type(null);//账号类型 没用到
                txnBonus.setStation_id(computeBonus.getStationId());//油站编号
                txnBonus.setPost_id(computeBonus.getPosId());
                txnBonus.setStation_name(stationShortName);//油站名称
                txnBonus.setTxn_amt(totalPrice);//交易金额<-应该商品实际购买价格
                txnBonus.setChannel(channel);//渠道
                txnBonus.setTxn_ssn_ora(computeBonus.getReqSerialNo());//原流水号
                txnBonus.setTxn_date_ora(businessDate);//营业日期
                txnBonus.setPay_typeSet(payTypeSet);//支付类型
                txnBonus.setTxn_type("s");//交易类型
                txnBonus.setMachine_id(null);//加油机号
                if(SysConstant.GOODS_TYPE_NOT_OIL.equals(goodsType)){ //非油品
                    txnBonus.setGoods_ids(SysConstant.NOT_OIL_FLAG);//非油品
                    txnBonus.setGoods_name("非油品");//商品名称
                    txnBonus.setGoods_mid_type(SysConstant.NOT_OIL_FLAG);//商品中类
                }else{ //油品
                    txnBonus.setGoods_ids(goodsId);//商品编号
                    txnBonus.setGoods_name(goodsName);//商品名称
                    txnBonus.setGoods_mid_type(middleType);//商品中类
                }
                txnBonus.setGoods_lit_type(litType);//商品小类
                txnBonus.setGoods_num(goodsNumber);//	商品购买数量
                txnBonus.setGoods_count(goodsNumber);//	商品购买数量
                txnBonus.setGoods_unit_price(unitPrice);//	商品单价
                txnBonus.setGoods_act_price(totalPrice);//商品实际购买价格
                txnBonus.setCust_level(custInf.getCustLevel());//客户级别
                double levelCoefficientValue = 1d; //默认原价
                Double levelValueDict = levelCoefficient.get(custInf.getCustLevel());
                if(levelValueDict != null){
                    levelCoefficientValue = levelValueDict;
                }
                txnBonus.setCust_level_coefficient(levelCoefficientValue);//级别系数
                txnBonus.setMarketregion(region);
                txnBonus.setField5(computeBonus.getPosId());
                txnBonus.setField1(String.valueOf(custInf.getIsRealCust()));//真我会员

                if(isWhite){
                    txnBonus.setAcct_id(unionPayCardNo);
                    txnBonus.setField3("1");// 银联卡号在白名单中
                }

                if(StrUtil.isNotBlank(unionPayCardNo) && CollUtil.contains(diyCadBinValue, StrUtil.sub(unionPayCardNo, cardBinStart, cardBinEnd))){
                    txnBonus.setField4("1");//是银联支付且卡bin满足自定义的卡bin
                }

                txnBonus.setField2(label);//会员标签
            }

            if(StrUtil.isNotBlank(birthday)){
                txnBonus.setBrithday(birthday);
            }
            txnBonus.convertData();//转换日期

            if(SysConstant.GOODS_TYPE_NOT_OIL.equals(goodsType)){
                tempTxnBonusMap.put(SysConstant.NOT_OIL_FLAG, txnBonus);
            }else{
                tempTxnBonusMap.put(txnBonus.getGoods_mid_type(), txnBonus);
            }
        }

        for (String midTypeId : tempTxnBonusMap.keySet()) {
            txnBonusList.add(tempTxnBonusMap.get(midTypeId));
        }
        return txnBonusList;
    }

    /**
     *
     *<p><strong>Description:</strong> 取出积分计算结果  </p>
     * @return
     * @author ZYK
     * @update 日期: 2015-5-6
     */
    private List<BonusAccItf> getResultFromTxnBonus(List<TxnBonus> txnBonusList) {
        List<BonusAccItf> bonusAccItfList = new ArrayList<BonusAccItf>();
        for(TxnBonus txnBonus:txnBonusList){
            List<PackageResult.ActivityResult> actResultList = txnBonus.getResult().activityGroupResults;
            if (actResultList != null && actResultList.size() > 0) {
                for (PackageResult.ActivityResult ar : actResultList) {
                    if (ar.result != null) {
                        if (ar.ruleGroupResults != null && ar.ruleGroupResults.size() > 0) {
                            for (int k = 0; k < ar.ruleGroupResults.size(); k++) {
                                PackageResult.RuleGroupResult grp = (PackageResult.RuleGroupResult) ar.ruleGroupResults.get(k);
                                bonusAccItfList = printGrpResult(bonusAccItfList, txnBonus, grp,ar);
                            }
                        }
                    }
                }

            }
        }

        return bonusAccItfList;
    }

    /**
     *
     *<p><strong>Description:</strong>  递归遍历规则结果 </p>
     * @param txnBonus
     * @param v_result
     * @param ar
     * @throws IOException
     * @author <a href="mailto: zhan_yaokang@huateng.com">zhanyaokang</a>
     * @throws SQLException
     * @update 日期: 2012-9-25
     */
    private List<BonusAccItf> printGrpResult(List<BonusAccItf> bonusAccItfList, TxnBonus txnBonus,PackageResult.RuleGroupResult v_result, PackageResult.ActivityResult ar){
        if(v_result != null){
            if (v_result.results != null&&v_result.results.size()>0) {
                int size = v_result.results.size();
                for (int i = 0; i < size; i++) {
                    TxnBonusResult rt = (TxnBonusResult) v_result.results.get(i);
                    bonusAccItfList =printResult(bonusAccItfList, txnBonus, rt,ar);
                }
            }
            bonusAccItfList = printGrpResult(bonusAccItfList, txnBonus, v_result.ruleGroupResult,ar);
        }
        return bonusAccItfList;

    }

    /**
     *
     *<p><strong>Description:</strong> 输出规则结果  </p>
     * @param txnBonus
     * @param result
     * @param ar
     * @throws IOException
     * @author <a href="mailto: zhan_yaokang@huateng.com">zhanyaokang</a>
     * @throws SQLException
     * @update 日期: 2012-9-25
     */
    private List<BonusAccItf>  printResult(List<BonusAccItf> bonusAccItfList, TxnBonus txnBonus,TxnBonusResult result, PackageResult.ActivityResult ar) {
        //int bonusPoint = result.getRoundBonusPoint();//产的积分
        BigDecimal bonusPoint = result.getRoundBonusPoint();//产的积分
        if (result.isExec && result.isEffect && bonusPoint.compareTo(BigDecimal.ZERO) > 0) {
            if (result.isExec && result.isEffect) {
                String activeId = ar.name != null ? ar.name.substring(2) : "";
                String ruleId = result.ruleName != null ? result.ruleName.substring(2) : "";

                BonusAccItf bonusAccItf = new BonusAccItf();
                bonusAccItf.setActivityId(activeId);//满足活动
                bonusAccItf.setRuleId(ruleId);//满足规则
                bonusAccItf.setGoodsId(txnBonus.getGoods_ids());//商品id
                bonusAccItf.setGoodsName(txnBonus.getGoods_name());//商品名称
                bonusAccItf.setTxnBonus(bonusPoint);//产生积分
                //bonusAccItf.setTxnBonus(NumberUtil.round(bonusPoint,2, RoundingMode.HALF_DOWN));//产生积分 保留两位小数
                bonusAccItf.setValidDate(result.validDate);//积分有效期
                bonusAccItf.setBpPlanType(result.bpPlanType);//积分计划
                bonusAccItf.setTxnAmtOra(NumberUtil.round(txnBonus.getTxn_amt(),4, RoundingMode.HALF_DOWN));// txn_amt_ora 原交易金额

                bonusAccItfList.add(bonusAccItf);
            }
        }
        return bonusAccItfList;
    }

    /**
     *  积分入账
     * @param bonusEnter
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public AddPointsResult bonusPlanEnter(BonusEnter bonusEnter) throws Exception{
        TblCustInf custInf = bonusEnter.getCustInf();//客户信息
        String custId = custInf.getCustId();
        String lockName = SysConstant.LOCK_MEMBER_POINTS + custId;
        RLock rLock = this.redissonClient.getLock(lockName);
        BigDecimal totalTxnPoints = BigDecimal.ZERO;//总产生积分

        /**
         * 重复提交订单锁控制
         */
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(SysConstant.LOCK_SERVICE_ORDER);
        stringBuilder.append(bonusEnter.getOrder().getChannel()).append(SysConstant.LOCK_SPLIT)
                .append(bonusEnter.getOrder().getReqSerialNo()).append(SysConstant.LOCK_SPLIT)
                .append(bonusEnter.getOrder().getOrderType());

        RLock orderLock = this.redissonClient.getLock(stringBuilder.toString());
        try {
            boolean isOk = rLock.tryLock(this.apolloBean.getRedissonTimeWait(), this.apolloBean.getRedissonTimeExpired(), TimeUnit.SECONDS);
            if (!isOk) {
                throw new BusinessException(getErrorInfo(SysConstant.SYS_LOCK_ERROR));
            }

            isOk = orderLock.tryLock(this.apolloBean.getRedissonTimeWait(), this.apolloBean.getRedissonTimeExpired(), TimeUnit.SECONDS);
            if (!isOk) {
                throw new BusinessException(getErrorInfo(SysConstant.SYS_LOCK_ERROR));
            }

            /**查询订单是否成功**/
            OrderInfo orderInfo = new OrderInfo();
            orderInfo.setOrderType(bonusEnter.getOrder().getOrderType());
            orderInfo.setChannel(bonusEnter.getOrder().getChannel());
            orderInfo.setReqSerialNo(bonusEnter.getOrder().getReqSerialNo());
            ServiceOrder serviceOrder = this.memberPointsRelateService.getOrderInfo(orderInfo);
            //订单成功状态返回原来的交易结果
            if (serviceOrder != null && SysConstant.ORDER_STATUS_SUCCESS.equals(serviceOrder.getStatus())) {
                BigDecimal expireBonus = null;
                String expireDate = DateUtil.date2String(serviceOrder.getHostDate(), DateUtil.DATE_YEAR) + SysConstant.YEAR_FLAG;
                List<String> validates = new ArrayList<>();
                validates.add(expireDate);

                BonusPlanDetailParams bonusPlanDetailParams = new BonusPlanDetailParams();
                bonusPlanDetailParams.setCustId(custInf.getCustId());
                bonusPlanDetailParams.setBpPlanType(SysConstant.BP_PLAN_TYPE_DEFAULT);
                bonusPlanDetailParams.setValidates(validates);
                ResInfo planDetailsResInfo = this.queryMemberPointsRemote.queryBonusPlanDetail(bonusPlanDetailParams);
                List<TblBonusPlanDetail> bonusPlanDetailList = JacksonUtil.toObject(getResJson(planDetailsResInfo),new TypeReference<List<TblBonusPlanDetail>>(){});
                if(CollUtil.isNotEmpty(bonusPlanDetailList)){
                    TblBonusPlanDetail planDetail = bonusPlanDetailList.get(0);
                    if(expireDate.equals(planDetail.getValidDate())){
                        expireBonus = planDetail.getValidBonus();
                    }
                }
                bonusPlanDetailParams = null;
                planDetailsResInfo = null;
                validates = null;
                bonusPlanDetailList = null;

                AddPointsResult result = new AddPointsResult();
                result.setCustId(custInf.getCustId());
                result.setTaxName(custInf.getCustInvoice());
                result.setIsAcceptEInvoice(custInf.getIsAcceptEinvoice());
                result.setValidBonus(serviceOrder.getValidAfter());
                result.setTxnBonus(serviceOrder.getNumber());
                result.setWillExpireBonus(NumberUtil.null2Zero(expireBonus));
                result.setWillExpireDate(expireDate);
                log.info("补传原单成功返回原单处理信息：{}",serviceOrder.getReqSerialNo());
                return result;
            }

            AddPointsResult addPointsResult = new AddPointsResult();//返回结果
            ServiceOrder order = bonusEnter.getOrder();//交易订单
            List<BonusAccItf> bonus = bonusEnter.getBonus();//积分计算信息
            List<Goods> orderGoods = bonusEnter.getGoods();//客户提交商品
            String txnDate = DateUtil.date2String(order.getChannelDate(), DateUtil.DATE_FORMAT_COMPACT);//交易日期
            String txnTime = DateUtil.date2String(order.getChannelDate(), DateUtil.DATE_FORMAT_TIME);//交易时间
            String expireDate = DateUtil.date2String(order.getHostDate(), DateUtil.DATE_YEAR) + SysConstant.YEAR_FLAG;//即将过期的有效期
            BigDecimal expireBonus = null;//即将过期的分数

            if(CollUtil.isNotEmpty(bonus)){
                totalTxnPoints = bonus.stream().map(BonusAccItf::getTxnBonus).reduce(BigDecimal.ZERO,BigDecimal::add);
            }

            this.validateService.validateForObject(this.apolloControl.getLimitDay(),SysConstant.E02000012);//日累计开关错误

           //更新日累计
            String custType = custInf.getCustType();//企业会员E不限制积分次数
            if(totalTxnPoints.compareTo(BigDecimal.ZERO) > 0 && this.apolloControl.getLimitDay().equals(1) && !SysConstant.CUST_TYPE_ENTERPRISE.equals(custType)){
                BigDecimal limitDayCount = this.apolloControl.getLimitDayCount();
                String hostDate = DateUtil.getCurrentDateyyyyMMdd();
                ServiceCustLimit serviceCustLimit = new ServiceCustLimit();
                serviceCustLimit.setLimitType(SysConstant.CUSTOMER_LIMIT_TYPE_DAY);//限制类型
                serviceCustLimit.setCustId(custId);//客户编号
                ResInfo resInfo = this.queryLimitRemote.queryLimit(serviceCustLimit);//查询日累计
                ServiceCustLimit limit = JacksonUtil.toObject(getResJson(resInfo),ServiceCustLimit.class);
                if(limit == null){
                    ServiceCustLimit saveLimit = new ServiceCustLimit();
                    saveLimit.setLimitType(serviceCustLimit.getLimitType());//限制类型
                    saveLimit.setCustId(serviceCustLimit.getCustId());//客户编号
                    saveLimit.setLimitKey(hostDate);
                    saveLimit.setLimitValue(BigDecimal.ONE);
                    serviceCustLimitMapper.insert(saveLimit);
                }else{
                    if(hostDate.equals(limit.getLimitKey())){
                        BigDecimal limitNewValue = limit.getLimitValue().add(BigDecimal.ONE);
                        if(limitNewValue.compareTo(limitDayCount) > 0){
                            throw new BusinessException(getErrorInfo(SysConstant.E02000013),new Object[]{custId, limitDayCount}); //积分日累计次数超出限制
                        }
                        limit.setLimitValue(limitNewValue);
                        serviceCustLimitMapper.updateByPrimaryKeySelective(limit);
                    }else{
                        //新日期设置为1
                        limit.setLimitKey(hostDate);
                        limit.setLimitValue(BigDecimal.ONE);
                        serviceCustLimitMapper.updateByPrimaryKeySelective(limit);
                    }

                }
                serviceCustLimit = null;
                resInfo = null;
                limit = null;
            }

            //客户积分计划
            ResInfo planResInfo = queryMemberPointsRemote.queryBonusPlanByCustId(custId, SysConstant.BP_PLAN_TYPE_DEFAULT);
            TblBonusPlan bonusPlan = JacksonUtil.toObject(getResJson(planResInfo), TblBonusPlan.class);
            planResInfo = null;
            if(bonusPlan == null){
                log.info("客户[{}]没有对应的积分计划[{}]的TblBonusPlan数据，自动创建",custId,SysConstant.DEFAULT_USAGE_KEY);
                bonusPlan = new TblBonusPlan();
                bonusPlan.setUsageKey(SysConstant.DEFAULT_USAGE_KEY);
                bonusPlan.setCustId(custId);
                bonusPlan.setLockStatus(SysConstant.BONUS_PLAN_LOCK_STATUS_NORMAL);//账户锁定状态
                bonusPlan.setBpPlanType(SysConstant.BP_PLAN_TYPE_DEFAULT);//积分计划
                bonusPlan.setTotalBonus(BigDecimal.ZERO);//总有效积分
                bonusPlan.setValidBonus(BigDecimal.ZERO);//有效积分
                bonusPlan.setApplyBonus(BigDecimal.ZERO);//已用积分
                bonusPlan.setExpireBonus(BigDecimal.ZERO);//失效积分
            }

            //客户积分详情
            Set<String> updateValidate = new HashSet<>();//操作的积分有效期
            Map<String,TblBonusPlanDetail> bonusPlanDetailMap = new HashMap<>();
            BonusPlanDetailParams bonusPlanDetailParams = new BonusPlanDetailParams();
            bonusPlanDetailParams.setCustId(custId);
            bonusPlanDetailParams.setBpPlanType(SysConstant.BP_PLAN_TYPE_DEFAULT);
            ResInfo planDetailsResInfo = this.queryMemberPointsRemote.queryBonusPlanDetail(bonusPlanDetailParams);
            List<TblBonusPlanDetail> bonusPlanDetailList = JacksonUtil.toObject(getResJson(planDetailsResInfo),new TypeReference<List<TblBonusPlanDetail>>(){});
            if(bonusPlanDetailList != null){
                bonusPlanDetailMap = bonusPlanDetailList.stream().collect(Collectors.toMap(TblBonusPlanDetail::getValidDate, Function.identity()));
            }
            bonusPlanDetailParams = null;
            planDetailsResInfo = null;
            bonusPlanDetailList = null;

            //积分自动调整
            Map<String, ServiceDict> dictBonusDelay = getServiceDict().get(SysConstant.DICT_KEY_1005000);
            ServiceDict bonusDelay = dictBonusDelay.get(SysConstant.DICT_BONUS_DELAY);
            BigDecimal bonusDelayValue = new BigDecimal(bonusDelay.getDictValue());
            if(totalTxnPoints.compareTo(bonusDelayValue) > 0){
                ResInfo resInfo = queryMemberPointsRemote.queryCustBonusDelay(custId);
                TblBonusDelay tblBonusDelay = JacksonUtil.toObject(getResJson(resInfo), TblBonusDelay.class);
                resInfo = null;
                if(tblBonusDelay != null){
                    if(SysConstant.AUTO_REISSUE_BONUS_DELAY.equals(tblBonusDelay.getStatus())){
                        log.info("开始处理TblBonusDelay...");
                        ServiceOrder delayOrder = new ServiceOrder();
                        BigDecimal txnBonus = tblBonusDelay.getTxnBonus();//待发积分
                        String validDate = tblBonusDelay.getValidDate();//待发有效期
                        Short operate = SysConstant.BONUS_FLAG_D.equals(tblBonusDelay.getBonusCdFlag()) ? SysConstant.BONUS_OPERATE_INCREASE : SysConstant.BONUS_OPERATE_DECREASE;
                        String msg = SysConstant.BONUS_FLAG_D.equals(tblBonusDelay.getBonusCdFlag()) ? SysConstant.MEMBER_POINTS_INCREASE_TYPE : SysConstant.MEMBER_POINTS_DECREASE_TYPE;
                        //操作之前
                        delayOrder.setValidBefore(bonusPlan.getValidBonus());
                        //操作积分账户
                        TblBonusPlanDetail planDetail = this.getPlanDetail(custId, SysConstant.BP_PLAN_TYPE_DEFAULT, bonusPlanDetailMap, validDate);
                        this.operatePlanAndDetail(bonusPlan,planDetail,operate,txnBonus);
                        updateValidate.add(validDate);
                        //操作之后
                        delayOrder.setValidAfter(bonusPlan.getValidBonus());

                        //添加订单
                        String orderId = getSnowId();
                        delayOrder.setId(orderId);//订单编号
                        delayOrder.setChannel(SysConstant.SYSTEM_CHANNEL);//默认总部渠道号
                        delayOrder.setPosId(SysConstant.SYSTEM_CHANNEL);//pos编号
                        delayOrder.setStationId(order.getStationId());//使用消费的油站编号
                        delayOrder.setReqSerialNo(tblBonusDelay.getId());//与待发表关联
                        delayOrder.setChannelDate(order.getChannelDate());//渠道日期
                        delayOrder.setBusinessDate(order.getBusinessDate());//营业日期
                        delayOrder.setHostDate(order.getHostDate());//主机日期
                        delayOrder.setNumber(txnBonus);//交易总积分
                        delayOrder.setReturnableNumber(txnBonus);//可退积分
                        delayOrder.setStatus(SysConstant.ORDER_STATUS_SUCCESS);//订单状态
                        delayOrder.setOrderType(SysConstant.ORDER_TYPE_REISSUE);//订单类型
                        delayOrder.setOperate(operate);
                        delayOrder.setCustId(custId);//custId
                        delayOrder.setAcctId(order.getAcctId());
                        serviceOrderMapper.insert(delayOrder);

                        //把积分调整信息当成商品名称保存
                        Map<String, ServiceDict> dictMap = getServiceDict().get(SysConstant.DICT_KEY_1004000);
                        ServiceDict serviceDictAdjust = dictMap.get(SysConstant.DICT_KEY_POINTS_ADJUST);
                        String goodsName = MessageFormat.format(serviceDictAdjust.getDictValue(),
                                new Object[]{
                                        tblBonusDelay.getAdjustDesc(),//原因
                                        msg,//操作
                                        tblBonusDelay.getTxnBonus()//调整的积分
                                });
                        ServiceOrderDetail delayOrderDetail = new ServiceOrderDetail();
                        delayOrderDetail.setId(tblBonusDelay.getId());//与待发表关联
                        delayOrderDetail.setOrderId(orderId);
                        delayOrderDetail.setGoodsId(String.valueOf(orderId));//把订单id当成商品id
                        delayOrderDetail.setTotalPrice(txnBonus);
                        delayOrderDetail.setUnitPrice(txnBonus);
                        delayOrderDetail.setNumber(BigDecimal.ONE);
                        delayOrderDetail.setReturnableNumber(BigDecimal.ONE);
                        delayOrderDetail.setGoodsName(goodsName);
                        delayOrderDetail.setGoodsType(SysConstant.GOODS_TYPE_REISSUE);
                        this.serviceOrderDetailMapper.insert(delayOrderDetail);

                        //保存积分流水
                        ServiceBonusDetail newServiceBonusDetail = new ServiceBonusDetail();
                        newServiceBonusDetail.setOrderId(orderId);
                        newServiceBonusDetail.setOrderSerial(0);
                        newServiceBonusDetail.setOperate(operate);
                        newServiceBonusDetail.setValidDate(validDate);
                        newServiceBonusDetail.setNumber(txnBonus);
                        newServiceBonusDetail.setReturnableNumber(txnBonus);
                        this.serviceBonusDetailMapper.insert(newServiceBonusDetail);

                        //更新自动补发数据
                        tblBonusDelay.setModifyDate(DateUtil.date2String(order.getBusinessDate(),DateUtil.DATE_FORMAT_COMPACT));
                        tblBonusDelay.setModifyTime(txnTime);
                        tblBonusDelay.setBonusSsn(order.getId()+"");
                        tblBonusDelay.setStatus("1");
                        tblBonusDelayMapper.updateStatus(tblBonusDelay);

                        //发送微信推送
                        SendWeChat sendWeChat = new SendWeChat();
                        sendWeChat.setTitle(SysConstant.MEMBER_POINTS_MSG_TITLE);
                        sendWeChat.setCode(tblBonusDelay.getStationId());
                        sendWeChat.setOpenId(custInf.getOpenId());
                        sendWeChat.setCardNo(custInf.getCustId());
                        sendWeChat.setCentent(tblBonusDelay.getAdjustDesc());
                        sendWeChat.setIntegral(tblBonusDelay.getTxnBonus());
                        sendWeChat.setAdjustFlag(msg);
                        addPointsResult.setDelayMsg(sendWeChat);
                    }
                }
            }

            //查询原订单商品信息
            ResInfo orderDetailResInfo = this.queryMemberPointsRemote.queryNewOrderDetailByOrderId(order.getId());
            List<ServiceOrderDetail> orderDetailList = JacksonUtil.toObject(getResJson(orderDetailResInfo), new TypeReference<List<ServiceOrderDetail>>(){});
            if(orderDetailList == null || orderDetailList.isEmpty()){
                //保存商品信息
                ServiceOrderDetail serviceOrderDetail;
//              TblFuelConsumption fuelConsumption;
                for (Goods goods : orderGoods) {
                    serviceOrderDetail = new ServiceOrderDetail();
                    serviceOrderDetail.setId(goods.getId());//子订单id
                    serviceOrderDetail.setOrderId(order.getId());//订单id
                    serviceOrderDetail.setGoodsId(goods.getGoodsId());//商品id
                    serviceOrderDetail.setTotalPrice(goods.getTotalPrice());//商品总价
                    serviceOrderDetail.setUnitPrice(goods.getUnitPrice());//商品单价
                    serviceOrderDetail.setMiddleType(String.valueOf(goods.getMiddleType()));//商品中类
                    serviceOrderDetail.setLitType(String.valueOf(goods.getLitType()));//商品小类
                    serviceOrderDetail.setNumber(goods.getNumber());//商品数量
                    serviceOrderDetail.setReturnableNumber(goods.getNumber());//可退数量
                    serviceOrderDetail.setGoodsName(goods.getGoodsName());//商品名称
                    serviceOrderDetail.setGoodsType(goods.getGoodsType());//商品类型
                    serviceOrderDetail.setDiscountType(goods.getDiscountType());//优惠类型
                    this.serviceOrderDetailMapper.insert(serviceOrderDetail);

                  //保存燃油信息 是油品并且 加油数量大于等于20L
//                if( (SysConstant.GOODS_TYPE_OIL.equals(goods.getGoodsType())) && (goods.getNumber().compareTo(new BigDecimal(20)) > -1) ){
//                    fuelConsumption = new TblFuelConsumption();
//                    fuelConsumption.setCustId(custId);
//                    fuelConsumption.setGoodsId(goods.getGoodsId());
//                    fuelConsumption.setGoodsNm(goods.getGoodsName());
//                    fuelConsumption.setGoodsNum(goods.getNumber());
//                    fuelConsumption.setGoodsTotalPrice(goods.getTotalPrice());
//                    fuelConsumption.setGoodsUnitPrice(goods.getUnitPrice());
//                    fuelConsumption.setTxnDate(txnDate);
//                    fuelConsumption.setTxnTime(txnTime);
//                    tblFuelConsumptionMapper.insert(fuelConsumption);
//                }

                }
            }

            //积分流水数据不变
            ServiceBonusDetail newServiceBonusDetail;
            int serialIndex = 0;
            if(bonus != null) {
                for (BonusAccItf bonusAccItf : bonus) {
                    //保存积分流水
                    String validDate = bonusAccItf.getValidDate();
                    BigDecimal txnBonus = bonusAccItf.getTxnBonus();
                    String ruleId = bonusAccItf.getRuleId();

                    newServiceBonusDetail = new ServiceBonusDetail();
                    newServiceBonusDetail.setOrderId(order.getId());
                    newServiceBonusDetail.setOrderSerial(serialIndex);
                    newServiceBonusDetail.setOperate(SysConstant.BONUS_OPERATE_INCREASE);
                    newServiceBonusDetail.setValidDate(validDate);
                    newServiceBonusDetail.setNumber(txnBonus);
                    newServiceBonusDetail.setReturnableNumber(txnBonus);
                    newServiceBonusDetail.setRuleId(ruleId);
                    this.serviceBonusDetailMapper.insert(newServiceBonusDetail);

                    serialIndex++;
                }
            }

            //积分自动进位需求
//            if(bonus != null){
//                BigDecimal newTotalTxnPoints = BigDecimal.ZERO;
//                Map<String,BonusAccItf> optMap = new HashMap<String,BonusAccItf>();
//                List<BonusAccItf> newBonusAccItfList = new ArrayList<BonusAccItf>();
//                //按有效期合计积分
//                for (BonusAccItf bonusAccItf : bonus) {
//                    String validDate = bonusAccItf.getValidDate();
//                    BonusAccItf date = optMap.get(validDate);
//                    if(date != null){
//                        date.setTxnBonus(date.getTxnBonus().add(bonusAccItf.getTxnBonus()));
//                        optMap.put(validDate,date);
//                    }else{
//                        optMap.put(validDate,bonusAccItf);
//                    }
//                }
//                //积分进位
//                for (String validate : optMap.keySet()) {
//                    BonusAccItf bonusAccItf = optMap.get(validate);
//                    BigDecimal roundPoints = NumberUtil.round(bonusAccItf.getTxnBonus(), 0, RoundingMode.CEILING);
//                    bonusAccItf.setTxnBonus(roundPoints);
//                    newBonusAccItfList.add(bonusAccItf);
//
//                    newTotalTxnPoints = newTotalTxnPoints.add(roundPoints);
//                }
//                bonus = newBonusAccItfList;
//                totalTxnPoints = newTotalTxnPoints;
//            }

            //积分入账
            order.setValidBefore(bonusPlan.getValidBonus());//操作前
            if(bonus != null) {
                for (BonusAccItf bonusAccItf : bonus) {
                    String validDate = bonusAccItf.getValidDate();//有效期
                    BigDecimal txnBonus = bonusAccItf.getTxnBonus();//分数
                    TblBonusPlanDetail planDetail = this.getPlanDetail(custId, SysConstant.BP_PLAN_TYPE_DEFAULT, bonusPlanDetailMap, validDate);
                    this.operatePlanAndDetail(bonusPlan, planDetail, SysConstant.BONUS_OPERATE_INCREASE, txnBonus);
                    updateValidate.add(validDate);
                }
            }
            order.setValidAfter(bonusPlan.getValidBonus());//操作后

            //更新订单
            order.setNumber(totalTxnPoints);
            order.setReturnableNumber(totalTxnPoints);
            order.setStatus(SysConstant.ORDER_STATUS_SUCCESS);
            this.serviceOrderMapper.updateByPrimaryKeySelective(order);

            //新增或更新积分有效期
            for (String valiDate : updateValidate) {
                TblBonusPlanDetail planDetail = bonusPlanDetailMap.get(valiDate);
                planDetail.setModifyOper(custId);
                planDetail.setModifyDate(txnDate);
                planDetail.setModifyTime(txnTime);
                if(planDetail.getPkBonusPlanDetail() == null){
                    planDetail.setCreateOper(SysConstant.DEFAULT_CREATE_OPER);
                    planDetail.setCreateDate(txnDate);
                    planDetail.setCreateTime(txnTime);
                    this.tblBonusPlanDetailMapper.insert(planDetail);
                }else{
                    this.tblBonusPlanDetailMapper.updatePlanDetailBonus(planDetail);
                }
            }

            //新增或更新积分总账
            bonusPlan.setModifyOper(custId);
            bonusPlan.setModifyDate(txnDate);
            bonusPlan.setModifyTime(txnTime);
            if(bonusPlan.getPkBonusPlan() == null){
                bonusPlan.setCreateOper(SysConstant.DEFAULT_CREATE_OPER);
                bonusPlan.setCreateDate(txnDate);
                bonusPlan.setCreateTime(txnTime);
                this.tblBonusPlanMapper.insert(bonusPlan);
            }else{
                this.tblBonusPlanMapper.updatePlanBonus(bonusPlan);
            }

            //入tbl_bonus_detail_account表 用于财务报表
            this.toRmsBonusData(bonusEnter);

            //发送微信推送
            if(totalTxnPoints.compareTo(BigDecimal.ZERO) > 0){
                SendWeChat sendWeChat = new SendWeChat();
                sendWeChat.setTitle(SysConstant.MEMBER_POINTS_MSG_TITLE);
                sendWeChat.setOpenId(custInf.getOpenId());
                sendWeChat.setCardNo(custId);
                sendWeChat.setCode(order.getStationId());
                sendWeChat.setIntegral(totalTxnPoints);
                sendWeChat.setDate(order.getChannelDate());
                sendWeChat.setTotalPoints(bonusPlan.getValidBonus());
                addPointsResult.setGenMsg(sendWeChat);
            }


            //即将过期的积分
            TblBonusPlanDetail planDetail = bonusPlanDetailMap.get(expireDate);
            if(planDetail != null){
                expireBonus = NumberUtil.null2Zero(planDetail.getValidBonus());
            }

            addPointsResult.setCustId(custId);//客户id
            addPointsResult.setTaxName(custInf.getCustInvoice());//发票台头
            addPointsResult.setIsAcceptEInvoice(custInf.getIsAcceptEinvoice());//是否接收自动开电子发票
            addPointsResult.setValidBonus(bonusPlan.getValidBonus());//当前有效期积分
            addPointsResult.setTxnBonus(totalTxnPoints);//交易积分
            addPointsResult.setWillExpireDate(expireDate);//即将过期的有效期
            addPointsResult.setWillExpireBonus(NumberUtil.null2Zero(expireBonus));//即将过期的积分
            return addPointsResult;
        }catch (Exception e){
            throw e;
        }finally {
            rLock.unlock();
            orderLock.unlock();
        }
    }

    /**
     * 入tbl_bonus_detail_account表
     * @param bonusEnter
     */
    private void toRmsBonusData(BonusEnter bonusEnter) throws Exception{
        List<TblBonusDetailAccount> dataList = new ArrayList<>();
        TblCustInf custInf = bonusEnter.getCustInf();
        ServiceOrder order = bonusEnter.getOrder();
        List<BonusAccItf> bonusResultList = bonusEnter.getBonus();//积分计算结果
        if(CollUtil.isNotEmpty(bonusResultList)){
            Map<Integer, ServiceNotProduceMidtype> notProduceMidtypeMap = this.getNotProduceMidtypeMap();
            List<Goods> goods = this.analysisNoPointsList(bonusEnter.getGoods(),notProduceMidtypeMap);
            Map<Short, List<Goods>> singleMap = goods.stream().collect(Collectors.groupingBy(Goods::getGoodsType)); //按油品、非油品分类
            for (BonusAccItf bonusAccItf : bonusResultList) {
                if(SysConstant.NOT_OIL_FLAG.equals(bonusAccItf.getGoodsId())){
                    /** 处理非油品 **/
                    List<Goods> notOilGoodsList = singleMap.get(SysConstant.GOODS_TYPE_NOT_OIL);
                    if(CollUtil.isNotEmpty(notOilGoodsList)){
                        BigDecimal surplusBonus = BigDecimal.ZERO;//已用积分 用于除不尽的情况
                        BigDecimal surplusPrice = BigDecimal.ZERO;//已用积分成本 用于除不尽的情况
                        BigDecimal txnBonus = bonusAccItf.getTxnBonus();//该非油品规则产生的总积分
                        BigDecimal txnPrice = bonusAccItf.getTxnBonus().multiply(SysConstant.BONUS_COST);//该非油品规则产生的总积分成本
                        BigDecimal txnAmtOra = bonusAccItf.getTxnAmtOra();//原交易金额
                        TblBonusDetailAccount tbda;
                        for (int i = 0; i < notOilGoodsList.size(); i++) {
                            Goods notOilGoods = notOilGoodsList.get(i);

                            tbda = new TblBonusDetailAccount();
                            tbda.setCustId(custInf.getCustId());
                            tbda.setBonusSsn(order.getId());
                            tbda.setStationId(order.getStationId());
                            tbda.setWorkDate(DateUtil.date2String(order.getBusinessDate(),DateUtil.DATE_FORMAT_COMPACT));
                            tbda.setShiftId(order.getShiftId());
                            tbda.setPosId(order.getPosId());
                            tbda.setListNo(order.getListNo());
                            tbda.setGoodsId(notOilGoods.getGoodsId());
                            tbda.setGoodsLitType(notOilGoods.getLitType().toString());
                            tbda.setGoodsNum(notOilGoods.getNumber());
                            tbda.setGoodsPrice(notOilGoods.getTotalPrice());
                            tbda.setBonusType("0"); //非油记油品的话记2
                            tbda.setRuleId(bonusAccItf.getRuleId());
                            tbda.setGoodsType("0");
                            //tbda.setRuleStationId();//限制使用油站

                            Double ratio = notOilGoods.getTotalPrice().doubleValue() / txnAmtOra.doubleValue();
                            BigDecimal userBonus;//该商品分摊的积分数
                            BigDecimal userPrice;//该商品分摊的积分成本
                            if(i == (notOilGoodsList.size() -1)){
                                userBonus = txnBonus.subtract(surplusBonus);
                                userPrice = txnPrice.subtract(surplusPrice);
                                tbda.setTxnBonus(userBonus);//分摊积分数
                                tbda.setTxnBonusPrice(userPrice);//分摊成本
                            }else{
                                userBonus = txnBonus.multiply(new BigDecimal(ratio)).setScale(2, BigDecimal.ROUND_HALF_UP);
                                userPrice = userBonus.multiply(SysConstant.BONUS_COST).setScale(2, BigDecimal.ROUND_HALF_UP);
                                tbda.setTxnBonus(userBonus);
                                tbda.setTxnBonusPrice(userPrice);
                            }
                            surplusBonus = surplusBonus.add(userBonus);
                            surplusPrice = surplusPrice.add(userPrice);

                            dataList.add(tbda);
                        }
                    }
                }else{
                    /** 处理油品 **/
                    List<Goods> oilGoodsList = singleMap.get(SysConstant.GOODS_TYPE_OIL);
                    if(CollUtil.isNotEmpty(oilGoodsList)){
                        for (int i = 0; i < oilGoodsList.size(); i++) {
                            Goods notOilGoods = oilGoodsList.get(i);
                            if(bonusAccItf.getGoodsId().equals(notOilGoods.getGoodsId())){
                                TblBonusDetailAccount tbda = new TblBonusDetailAccount();
                                tbda.setCustId(custInf.getCustId());
                                tbda.setBonusSsn(order.getId());
                                tbda.setStationId(order.getStationId());
                                tbda.setWorkDate(DateUtil.date2String(order.getBusinessDate(),DateUtil.DATE_FORMAT_COMPACT));
                                tbda.setShiftId(order.getShiftId());
                                tbda.setPosId(order.getPosId());
                                tbda.setListNo(order.getListNo());
                                tbda.setGoodsId(notOilGoods.getGoodsId());
                                tbda.setGoodsLitType(notOilGoods.getLitType().toString());
                                tbda.setGoodsNum(notOilGoods.getNumber().multiply(new BigDecimal("1000")));
                                tbda.setGoodsPrice(notOilGoods.getTotalPrice());
                                tbda.setBonusType("0");
                                tbda.setRuleId(bonusAccItf.getRuleId());
                                tbda.setGoodsType("1");
                                //tbda.setRuleStationId();//限制使用油站
                                tbda.setTxnBonus(bonusAccItf.getTxnBonus());//分摊积分数
                                tbda.setTxnBonusPrice(bonusAccItf.getTxnBonus().multiply(SysConstant.BONUS_COST));//分摊成本

                                dataList.add(tbda);
                            }
                        }
                    }
                }
            }
        }

        for (TblBonusDetailAccount tblBonusDetailAccount : dataList) {
            tblBonusDetailAccountMapper.insertSelective(tblBonusDetailAccount);
        }
    }


    //取有效期没有则创建并保持到map中
    private TblBonusPlanDetail getPlanDetail(String custId,String bpPlanType,Map<String,TblBonusPlanDetail> bonusPlanDetailMap,String validDate){
        TblBonusPlanDetail operatePlanDetail = bonusPlanDetailMap.get(validDate);
        if(operatePlanDetail == null){
            log.info("客户[{}]没有对应的积分计划[{}]有效期为[{}]的tblBonusPlanDetail数据，自动创建",custId,bpPlanType,validDate);
            operatePlanDetail = new TblBonusPlanDetail();
            operatePlanDetail.setUsageKey(SysConstant.DEFAULT_USAGE_KEY);
            operatePlanDetail.setCustId(custId);
            operatePlanDetail.setBpPlanType(bpPlanType);
            operatePlanDetail.setTotalBonus(BigDecimal.ZERO);
            operatePlanDetail.setValidBonus(BigDecimal.ZERO);
            operatePlanDetail.setApplyBonus(BigDecimal.ZERO);
            operatePlanDetail.setExpireBonus(BigDecimal.ZERO);
            operatePlanDetail.setValidDate(validDate);
            operatePlanDetail.setExpiredStatus(SysConstant.BONUS_PLAN_DETAIL_IN_EFFECT);
            operatePlanDetail.setLockStatus(SysConstant.BONUS_PLAN_DETAIL_LOCK_STATUS_NORMAL);

            bonusPlanDetailMap.put(validDate,operatePlanDetail);//保存
        }
        return operatePlanDetail;
    }
    //操作积分账户
    private void operatePlanAndDetail(TblBonusPlan bonusPlan,TblBonusPlanDetail bonusPlanDetail,Short operate,BigDecimal txnBonus){
        if (SysConstant.BONUS_OPERATE_INCREASE.equals(operate)){
            bonusPlanDetail.setTotalBonus(bonusPlanDetail.getTotalBonus().add(txnBonus));//积分累加
            bonusPlanDetail.setValidBonus(bonusPlanDetail.getValidBonus().add(txnBonus));//有效积分增加
            bonusPlan.setTotalBonus(bonusPlan.getTotalBonus().add(txnBonus));//积分累加
            bonusPlan.setValidBonus(bonusPlan.getValidBonus().add(txnBonus));//有效积分增加
        }else{
            bonusPlanDetail.setApplyBonus(bonusPlanDetail.getApplyBonus().add(txnBonus));//已用积分增加
            bonusPlanDetail.setValidBonus(bonusPlanDetail.getValidBonus().subtract(txnBonus));//可用减少
            bonusPlan.setApplyBonus(bonusPlan.getApplyBonus().add(txnBonus));//已用积分增加
            bonusPlan.setValidBonus(bonusPlan.getValidBonus().subtract(txnBonus));//可用减少
        }
    }

    /**
     * 积分冲正-整单冲正
     * @param autoBonusReversal
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public void bonusReversalAll(AutoBonusReversal autoBonusReversal) throws Exception{
        String lockName = SysConstant.LOCK_MEMBER_POINTS + autoBonusReversal.getCustId();
        RLock rLock = this.redissonClient.getLock(lockName);
        ServiceOrder serviceOrder;
        try {
            boolean isOk = rLock.tryLock(this.apolloBean.getRedissonTimeWait(), this.apolloBean.getRedissonTimeExpired(), TimeUnit.SECONDS);
            if (!isOk) {
                throw new BusinessException(getErrorInfo(SysConstant.SYS_LOCK_ERROR));//获取锁失败
            }

            /**原订单信息**/
            ResInfo orderResInfo = this.queryMemberPointsRemote.queryOrderById(autoBonusReversal.getTargetOrderId());
            ServiceOrder order = JacksonUtil.toObject(getResJson(orderResInfo), ServiceOrder.class);
            orderResInfo = null;

            /**原订单商品信息**/
            ResInfo orderDetailResInfo = this.queryMemberPointsRemote.queryNewOrderDetailByOrderId(order.getId());
            List<ServiceOrderDetail> orderDetailList = JacksonUtil.toObject(getResJson(orderDetailResInfo), new TypeReference<List<ServiceOrderDetail>>(){});
            orderDetailResInfo = null;

            /**原订单流水信息**/
            ResInfo bonusDetailResInfo = this.queryMemberPointsRemote.queryNewBonusDetailByOrderId(order.getId());
            List<ServiceBonusDetail> bonusDetailList = JacksonUtil.toObject(getResJson(bonusDetailResInfo), new TypeReference<List<ServiceBonusDetail>>(){});
            bonusDetailResInfo = null;

            String orderId = autoBonusReversal.getOrderId();//冲正订单id
            String hostDate = DateUtil.date2String(autoBonusReversal.getHostDateTime(), DateUtil.DATE_FORMAT_COMPACT);
            String hostTime = DateUtil.date2String(autoBonusReversal.getHostDateTime(), DateUtil.DATE_FORMAT_TIME);
            String yearFlag = DateUtil.getCurrentDateyyyy() + SysConstant.YEAR_FLAG;

            /**重复冲**/
            if(order.getReturnableNumber().compareTo(BigDecimal.ZERO) == 0){
                serviceOrder = new ServiceOrder();
                serviceOrder.setId(orderId);
                serviceOrder.setStatus(SysConstant.ORDER_STATUS_SUCCESS);
                this.serviceOrderMapper.updateByPrimaryKeySelective(serviceOrder);
                serviceOrder = null;
                return;
            }

            //会员积分账户
            ResInfo planResInfo = this.queryMemberPointsRemote.queryBonusPlanByCustId(autoBonusReversal.getCustId(),SysConstant.BP_PLAN_TYPE_DEFAULT);
            TblBonusPlan bonusPlan = JacksonUtil.toObject(getResJson(planResInfo), TblBonusPlan.class);
            planResInfo = null;
            BigDecimal validBefore = bonusPlan.getValidBonus();//操作前积分

            /*if(order.getOrderType().equals(SysConstant.ORDER_TYPE_PRODUCE)){//原单为积分产生时校验总账积分余额
                if (bonusPlan.getValidBonus().compareTo(autoBonusReversal.getReverseNumber()) < 0) {
                    throw new BusinessException(getErrorInfo(SysConstant.E02000004));//积分余额不足
                }
            }*/


            /**会员积分明细**/
            BonusPlanDetailParams bonusPlanDetailParams = new BonusPlanDetailParams();
            bonusPlanDetailParams.setCustId(autoBonusReversal.getCustId());
            bonusPlanDetailParams.setBpPlanType(SysConstant.BP_PLAN_TYPE_DEFAULT);
            bonusPlanDetailParams.setIsOrderByDate(1);
            ResInfo planDetailsResInfo = this.queryMemberPointsRemote.queryBonusPlanDetail(bonusPlanDetailParams);
            List<TblBonusPlanDetail> bonusPlanDetailList = JacksonUtil.toObject(getResJson(planDetailsResInfo),new TypeReference<List<TblBonusPlanDetail>>(){});
            Map<String, TblBonusPlanDetail> bonusPlanDetailMap = bonusPlanDetailList.stream().collect(Collectors.toMap(TblBonusPlanDetail::getValidDate, Function.identity()));
            bonusPlanDetailParams = null;
            planDetailsResInfo = null;
            bonusPlanDetailList = null;


            //并按有效期汇总进位
            List<ServiceBonusDetail> returnList = this.bonusAutoCeiling(bonusDetailList);
            //把原流水可退积分全置为0
            this.serviceBonusDetailMapper.updateReturnableNumberToZeroByOrderId(order.getId());

            //按照积分流水进行冲正
            BigDecimal extendValidateNumber = BigDecimal.ZERO;//需要延长有效期的分数
            int serial = 0;
            /**循环原订单积分流水**/
            for (ServiceBonusDetail bonusDetail : returnList) {
                String validDate = bonusDetail.getValidDate();//积分有效期
                BigDecimal bonusDetailNumber = bonusDetail.getReturnableNumber();//流水可退积分
                if(bonusDetailNumber.compareTo(BigDecimal.ZERO) == 0){ //流水可退积分为0直接跳过
                    continue;
                }

                TblBonusPlanDetail bonusPlanDetail = bonusPlanDetailMap.get(validDate);//根据有效期拿出客户的积分有效期详情
                boolean isOverdue = DateUtil.isBefore4LocalDate(validDate, yearFlag, DateUtil.DATE_FORMAT_COMPACT);//有效期是否已过期
                if(isOverdue){
                    bonusPlanDetail.setExpiredStatus(SysConstant.BONUS_PLAN_DETAIL_EXPIRED);//设置为过期已处理
                    bonusPlanDetail.setTotalBonus(bonusPlanDetail.getTotalBonus().subtract(bonusDetailNumber));
                    bonusPlanDetail.setApplyBonus(bonusPlanDetail.getApplyBonus().subtract(bonusDetailNumber));
                    extendValidateNumber = bonusDetailNumber;
                }else{
                    bonusPlanDetail.setApplyBonus(bonusPlanDetail.getApplyBonus().subtract(bonusDetailNumber));
                    bonusPlanDetail.setValidBonus(bonusPlanDetail.getValidBonus().add(bonusDetailNumber));
                }

                bonusPlan.setApplyBonus(bonusPlan.getApplyBonus().subtract(bonusDetailNumber));
                bonusPlan.setValidBonus(bonusPlan.getValidBonus().add(bonusDetailNumber));

                //更新客户有效期积分
                bonusPlanDetail.setModifyDate(hostDate);
                bonusPlanDetail.setModifyTime(hostTime);
                this.tblBonusPlanDetailMapper.updatePlanDetailBonus(bonusPlanDetail);

                if(!isOverdue){
                    //插入本次冲正的积分流水
                    ServiceBonusDetail serviceBonusDetail = new ServiceBonusDetail();
                    serviceBonusDetail.setOrderId(orderId);//冲正订单的id
                    serviceBonusDetail.setOrderSerial(serial);
                    serviceBonusDetail.setOperate(SysConstant.BONUS_OPERATE_INCREASE);
                    serviceBonusDetail.setValidDate(validDate);
                    serviceBonusDetail.setNumber(bonusDetailNumber);
                    this.serviceBonusDetailMapper.insert(serviceBonusDetail);
                }

                serial++;
            }

            //自动延期
            this.autoExtension(extendValidateNumber,bonusPlanDetailMap,yearFlag,hostDate,hostTime,autoBonusReversal.getCustId(),autoBonusReversal.getOrderId(),serial);

            //更新客户账户积分
            bonusPlan.setModifyDate(hostDate);
            bonusPlan.setModifyTime(hostTime);
            this.tblBonusPlanMapper.updatePlanBonus(bonusPlan);

            //更新目标订单商品可冲正数量，保存冲正订单商品
            for (ServiceOrderDetail orderDetail : orderDetailList) {
                BigDecimal returnableNumber = orderDetail.getReturnableNumber();
                if(returnableNumber.compareTo(BigDecimal.ZERO) == 0){//可退数量为0直接跳过
                    continue;
                }

                ServiceOrderDetail saveOrderDetail = new ServiceOrderDetail();
                saveOrderDetail.setId(orderDetail.getId());//子订单id
                saveOrderDetail.setOrderId(orderId);//冲正订单id
                saveOrderDetail.setGoodsId(orderDetail.getGoodsId());
                saveOrderDetail.setTotalPrice(orderDetail.getTotalPrice());
                saveOrderDetail.setUnitPrice(orderDetail.getUnitPrice());
                saveOrderDetail.setMiddleType(orderDetail.getMiddleType());
                saveOrderDetail.setLitType(orderDetail.getLitType());
                saveOrderDetail.setNumber(orderDetail.getReturnableNumber());
                saveOrderDetail.setGoodsName(orderDetail.getGoodsName());
                saveOrderDetail.setGoodsType(orderDetail.getGoodsType());
                saveOrderDetail.setDiscountType(orderDetail.getDiscountType());
                this.serviceOrderDetailMapper.insert(saveOrderDetail);

                orderDetail.setReturnableNumber(BigDecimal.ZERO);
                this.serviceOrderDetailMapper.updateReturnableNumber(orderDetail);//更新原单商品可退数量

            }
            //更新原订单可退积分
            serviceOrder = new ServiceOrder();
            serviceOrder.setId(autoBonusReversal.getTargetOrderId());
            serviceOrder.setStatus(SysConstant.ORDER_STATUS_REVERSAL_FULL);//状态为已全部冲正
            serviceOrder.setReturnableNumber(BigDecimal.ZERO);
            this.serviceOrderMapper.updateByPrimaryKeySelective(serviceOrder);


            //更新冲正订单为成功
            serviceOrder = new ServiceOrder();
            serviceOrder.setValidBefore(validBefore);//交易前积分数
            serviceOrder.setValidAfter(bonusPlan.getValidBonus());//交易后积分数
            serviceOrder.setId(orderId);
            serviceOrder.setNumber(order.getReturnableNumber());
            serviceOrder.setStatus(SysConstant.ORDER_STATUS_SUCCESS);
            this.serviceOrderMapper.updateByPrimaryKeySelective(serviceOrder);
        }catch (Exception e){
            throw e;
        }finally {
            rLock.unlock();
        }
    }

    /**
     * 并按有效期汇总进位
     * @param bonusDetailList
     * @return
     */
    private List<ServiceBonusDetail> bonusAutoCeiling(List<ServiceBonusDetail> bonusDetailList){
        List<ServiceBonusDetail> list = new ArrayList<>();

        Map<String,ServiceBonusDetail> map = new HashMap<>();
        for (ServiceBonusDetail serviceBonusDetail : bonusDetailList) {
            ServiceBonusDetail oldBonus = map.get(serviceBonusDetail.getValidDate());
            if(oldBonus == null){
                map.put(serviceBonusDetail.getValidDate(),serviceBonusDetail);
            }else{
                oldBonus.setReturnableNumber(oldBonus.getReturnableNumber().add(serviceBonusDetail.getReturnableNumber()));
                map.put(serviceBonusDetail.getValidDate(),oldBonus);
            }
        }
        for (String validDate : map.keySet()) {
            ServiceBonusDetail serviceBonusDetail = map.get(validDate);
            BigDecimal roundPoints = NumberUtil.round(serviceBonusDetail.getReturnableNumber(), 0, RoundingMode.CEILING);
            serviceBonusDetail.setReturnableNumber(roundPoints);

            list.add(serviceBonusDetail);
        }
        return list;
    }

    /**
     * 积分冲正-兼容老交易
     * @param autoTblBonusReversal
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public void tblBonusReversalAll(AutoTblBonusReversal autoTblBonusReversal)throws Exception{
        String lockName = SysConstant.LOCK_MEMBER_POINTS + autoTblBonusReversal.getCustId();
        RLock rLock = this.redissonClient.getLock(lockName);
        ServiceOrder serviceOrder;
        try {
            boolean isOk = rLock.tryLock(this.apolloBean.getRedissonTimeWait(), this.apolloBean.getRedissonTimeExpired(), TimeUnit.SECONDS);
            if (!isOk) {
                throw new BusinessException(getErrorInfo(SysConstant.SYS_LOCK_ERROR));//获取锁失败
            }

            ResInfo orderResInfo = this.queryMemberPointsRemote.queryTblOrderByOrderId(autoTblBonusReversal.getTargetOrderId());
            TblOrder order = JacksonUtil.toObject(getResJson(orderResInfo),TblOrder.class);//老原订单
            orderResInfo = null;

            ResInfo txnDetailResInfo = this.queryMemberPointsRemote.queryTxnDetailByAcqSsn(order.getAcqSsn());
            TblTxnDetail txnDetail = JacksonUtil.toObject(getResJson(txnDetailResInfo),TblTxnDetail.class);//原单交易流水
            txnDetailResInfo = null;

            ResInfo tblOrderDetailResInfo = this.queryMemberPointsRemote.queryOrderDetailByOrderId(order.getOrderId());
            List<TblOrderDetail> tblOrderDetailList = JacksonUtil.toObject(getResJson(tblOrderDetailResInfo),new TypeReference<List<TblOrderDetail>>(){});//原单商品
            tblOrderDetailResInfo = null;

            ResInfo tblBonusDetailResInfo = this.queryMemberPointsRemote.queryBonusDetailByBonusSsn(order.getBonusSsn());
            List<TblBonusDetail> tblBonusDetailList = JacksonUtil.toObject(getResJson(tblBonusDetailResInfo),new TypeReference<List<TblBonusDetail>>(){});//原单积分流水
            tblBonusDetailResInfo = null;

            String orderId = autoTblBonusReversal.getOrderId();//冲正订单id
            String custId = autoTblBonusReversal.getCustId();//会员id
            String hostDate = DateUtil.date2String(autoTblBonusReversal.getHostDateTime(), DateUtil.DATE_FORMAT_COMPACT);//主机日期
            String hostTime = DateUtil.date2String(autoTblBonusReversal.getHostDateTime(), DateUtil.DATE_FORMAT_TIME);//主机时间
            String yearFlag = DateUtil.getCurrentDateyyyy() + SysConstant.YEAR_FLAG;

            /**重复冲**/
            if(order.getOrderBonus().compareTo(BigDecimal.ZERO) == 0){
                serviceOrder = new ServiceOrder();
                serviceOrder.setId(autoTblBonusReversal.getOrderId());
                serviceOrder.setStatus(SysConstant.ORDER_STATUS_SUCCESS);
                this.serviceOrderMapper.updateByPrimaryKeySelective(serviceOrder);
                serviceOrder = null;
                return;
            }

            //客户积分计划
            ResInfo planResInfo = queryMemberPointsRemote.queryBonusPlanByCustId(custId, SysConstant.BP_PLAN_TYPE_DEFAULT);
            TblBonusPlan bonusPlan = JacksonUtil.toObject(getResJson(planResInfo), TblBonusPlan.class);
            planResInfo = null;
            serviceOrder = new ServiceOrder();
            serviceOrder.setValidBefore(bonusPlan.getValidBonus());//交易前分数

            //客户积分详情
            BonusPlanDetailParams bonusPlanDetailParams = new BonusPlanDetailParams();
            bonusPlanDetailParams.setCustId(custId);
            bonusPlanDetailParams.setBpPlanType(SysConstant.BP_PLAN_TYPE_DEFAULT);
            ResInfo planDetailsResInfo = this.queryMemberPointsRemote.queryBonusPlanDetail(bonusPlanDetailParams);
            List<TblBonusPlanDetail> bonusPlanDetailList = JacksonUtil.toObject(getResJson(planDetailsResInfo),new TypeReference<List<TblBonusPlanDetail>>(){});
            Map<String, TblBonusPlanDetail> bonusPlanDetailMap = bonusPlanDetailList.stream().collect(Collectors.toMap(TblBonusPlanDetail::getValidDate, Function.identity()));
            bonusPlanDetailParams = null;
            planDetailsResInfo = null;

            //原单积分流水
            int serialIndex = 0;
            TblBonusDetail tblBonusDetailParam;
            BigDecimal extendValidateNumber = BigDecimal.ZERO;
            Short operate = null;
            for (TblBonusDetail tblBonusDetail : tblBonusDetailList) {
                String validDate = tblBonusDetail.getValidDate();//原流水有效期
                String bonusCdFlag = tblBonusDetail.getBonusCdFlag();//原流水操作c积分减少，d积分增加
                BigDecimal txnBonus = tblBonusDetail.getTxnBonus();//积分数量
                if(txnBonus.compareTo(BigDecimal.ZERO) == 0){ //流水可退积分为0直接跳过
                    continue;
                }

                TblBonusPlanDetail planDetail = bonusPlanDetailMap.get(validDate);
                operate = "c".equals(bonusCdFlag) ? SysConstant.BONUS_OPERATE_INCREASE : SysConstant.BONUS_OPERATE_DECREASE; //取反操作
                boolean isOverdue = DateUtil.isBefore4LocalDate(validDate, yearFlag, DateUtil.DATE_FORMAT_COMPACT);//冲正的流水有效期是否已过期

                boolean isExtend = false;//是否有自动延长有效期操作
                if(SysConstant.BONUS_OPERATE_INCREASE.equals(operate)){ //冲正为加积分操作
                    if(isOverdue){
                        isExtend = true;
                        planDetail.setExpiredStatus(SysConstant.BONUS_PLAN_DETAIL_EXPIRED);
                        planDetail.setTotalBonus(planDetail.getTotalBonus().subtract(txnBonus));
                        planDetail.setApplyBonus(planDetail.getApplyBonus().subtract(txnBonus));
                        extendValidateNumber = extendValidateNumber.add(txnBonus);
                    }else{
                        planDetail.setApplyBonus(planDetail.getApplyBonus().subtract(txnBonus));
                        planDetail.setValidBonus(planDetail.getValidBonus().add(txnBonus));
                    }

                    bonusPlan.setApplyBonus(bonusPlan.getApplyBonus().subtract(txnBonus));
                    bonusPlan.setValidBonus(bonusPlan.getValidBonus().add(txnBonus));
                }else{//冲正为减积分操作
                    planDetail.setTotalBonus(planDetail.getTotalBonus().subtract(txnBonus));
                    planDetail.setValidBonus(planDetail.getValidBonus().subtract(txnBonus));

                    bonusPlan.setTotalBonus(bonusPlan.getTotalBonus().subtract(txnBonus));
                    bonusPlan.setValidBonus(bonusPlan.getValidBonus().subtract(txnBonus));
                }

                //更新有效期积分
                planDetail.setModifyDate(hostDate);
                planDetail.setModifyTime(hostTime);
                this.tblBonusPlanDetailMapper.updatePlanDetailBonus(planDetail);

                if(!isExtend){
                    ServiceBonusDetail newServiceBonusDetail = new ServiceBonusDetail();
                    newServiceBonusDetail.setOrderId(orderId);
                    newServiceBonusDetail.setOrderSerial(serialIndex);
                    newServiceBonusDetail.setOperate(operate);
                    newServiceBonusDetail.setValidDate(validDate);
                    newServiceBonusDetail.setNumber(txnBonus);
                    newServiceBonusDetail.setReturnableNumber(txnBonus);
                    this.serviceBonusDetailMapper.insert(newServiceBonusDetail);
                }

                //更新原流水分数
                tblBonusDetailParam = new TblBonusDetail();
                tblBonusDetailParam.setPkBonusDetail(tblBonusDetail.getPkBonusDetail());
                tblBonusDetailParam.setReturnFlag("1");
                this.tblBonusDetailMapper.updateReturnFlagByPrimaryKey(tblBonusDetailParam);
                serialIndex++;
            }
            tblBonusDetailParam = null;

            //自动延期
            this.autoExtension(extendValidateNumber,bonusPlanDetailMap,yearFlag,hostDate,hostTime,custId,orderId,serialIndex);

            //更新客户账户积分
            bonusPlan.setModifyDate(hostDate);
            bonusPlan.setModifyTime(hostTime);
            this.tblBonusPlanMapper.updatePlanBonus(bonusPlan);

            //原单商品
            TblOrderDetail updateOrderDetailParam;
            for (TblOrderDetail tblOrderDetail : tblOrderDetailList) {
                BigDecimal orderNum = tblOrderDetail.getOrderNum();//商品可退数量
                if(BigDecimal.ZERO.compareTo(orderNum) == 0){
                    continue;
                }
                ServiceOrderDetail orderDetailAll = new ServiceOrderDetail();
                orderDetailAll.setId(tblOrderDetail.getOrderDetailId());
                orderDetailAll.setOrderId(orderId);
                orderDetailAll.setGoodsId(tblOrderDetail.getGoodsId());
                orderDetailAll.setNumber(orderNum);
                orderDetailAll.setReturnableNumber(orderNum);
                orderDetailAll.setGoodsName(tblOrderDetail.getGoodsNm());
                orderDetailAll.setGoodsType(SysConstant.GOODS_TYPE_NOT_OIL);
                orderDetailAll.setTotalPrice(tblOrderDetail.getOrderBonus());
                orderDetailAll.setUnitPrice(BigDecimal.ZERO);
                this.serviceOrderDetailMapper.insert(orderDetailAll);

                updateOrderDetailParam = new TblOrderDetail();
                updateOrderDetailParam.setPkOrderDtl(tblOrderDetail.getPkOrderDtl());
                updateOrderDetailParam.setReturnTime(hostDate);
                updateOrderDetailParam.setOrderCancel("0");
                this.tblOrderDetailMapper.updateByPrimaryKeySelective(updateOrderDetailParam);
            }
            updateOrderDetailParam = null;

            //更新原单交易流水
            txnDetail.setReturnFlag("1");
            this.tblTxnDetailMapper.updateReturnFlag(txnDetail);

            //更新原单
            order.setOrderStatus("01");
            order.setReturnTime(DateUtil.date2String(autoTblBonusReversal.getHostDateTime(), DateUtil.DATE_FORMAT_COMPACT));
            this.tblOrderMapper.updateStatus(order);

            //更新冲正订单
            serviceOrder.setValidAfter(bonusPlan.getValidBonus());//交易后的分数
            serviceOrder.setId(orderId);
            serviceOrder.setOperate(operate);
            serviceOrder.setNumber(order.getOrderBonus());
            serviceOrder.setStatus(SysConstant.ORDER_STATUS_SUCCESS);
            this.serviceOrderMapper.updateByPrimaryKeySelective(serviceOrder);
        }catch (Exception e){
            throw e;
        }finally {
            rLock.unlock();
        }
    }

    /**
     * 积分冲正-部分冲正
     * @param autoBonusReversal
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public void bonusReversalPart(AutoBonusReversal autoBonusReversal) throws Exception{
        String lockName = SysConstant.LOCK_MEMBER_POINTS + autoBonusReversal.getCustId();
        RLock rLock = this.redissonClient.getLock(lockName);
        ServiceOrder serviceOrder;
        try {
            boolean isOk = rLock.tryLock(this.apolloBean.getRedissonTimeWait(), this.apolloBean.getRedissonTimeExpired(), TimeUnit.SECONDS);
            if (!isOk) {
                throw new BusinessException(getErrorInfo(SysConstant.SYS_LOCK_ERROR));//获取锁失败
            }

            /**原订单信息**/
            ResInfo orderResInfo = this.queryMemberPointsRemote.queryOrderById(autoBonusReversal.getTargetOrderId());
            ServiceOrder order = JacksonUtil.toObject(getResJson(orderResInfo), ServiceOrder.class);
            orderResInfo = null;

            /**原订单商品信息**/
            ResInfo orderDetailResInfo = this.queryMemberPointsRemote.queryNewOrderDetailByOrderId(order.getId());
            List<ServiceOrderDetail> orderDetailList = JacksonUtil.toObject(getResJson(orderDetailResInfo), new TypeReference<List<ServiceOrderDetail>>(){});
            orderDetailResInfo = null;

            /**原订单流水信息**/
            ResInfo bonusDetailResInfo = this.queryMemberPointsRemote.queryNewBonusDetailByOrderId(order.getId());
            List<ServiceBonusDetail> bonusDetailList = JacksonUtil.toObject(getResJson(bonusDetailResInfo), new TypeReference<List<ServiceBonusDetail>>(){});
            bonusDetailResInfo = null;

            String hostDate = DateUtil.date2String(autoBonusReversal.getHostDateTime(), DateUtil.DATE_FORMAT_COMPACT);//主机日期
            String hostTime = DateUtil.date2String(autoBonusReversal.getHostDateTime(), DateUtil.DATE_FORMAT_TIME);//主机时间
            String yearFlag = DateUtil.getCurrentDateyyyy()+SysConstant.YEAR_FLAG;//用于判断冲正积分是否已过期
            BigDecimal reverseNumber = autoBonusReversal.getReverseNumber();//待冲正积分
            String orderId = autoBonusReversal.getOrderId();//冲正订单id
            String custId = autoBonusReversal.getCustId();//会员id

            if(reverseNumber.compareTo(order.getReturnableNumber()) > 0){
                throw new BusinessException(getErrorInfo(SysConstant.E02000007));//冲正积分数不可大于原单可冲正积分数
            }

            //原订单商品转成map<子订单id,商品>
            Map<String, ServiceOrderDetail> orderDetailMap = orderDetailList.stream().collect(Collectors.toMap(ServiceOrderDetail::getId, Function.identity()));
            boolean flag = true;//是否有优惠商品
            BigDecimal returnNumber = BigDecimal.ZERO;//校验冲正分数与退货数量是否匹配
            for (ReversalDetail detail : autoBonusReversal.getDetails()) {
                String id = detail.getId();//冲正子订单id
                BigDecimal goodsNumber = detail.getNumber();//冲正商品数量
                ServiceOrderDetail orderDetail = orderDetailMap.get(id);//根据子订单id取出原单商品

                if(orderDetail == null){
                    throw new BusinessException(getErrorInfo(SysConstant.E02000011),new Object[]{id});//原订单中无{0}子订单号
                }

                //校验可退数量
                if(goodsNumber.compareTo(orderDetail.getReturnableNumber()) > 0){
                    throw new BusinessException(getErrorInfo(SysConstant.E02000010),new Object[]{id});//子订单号[{0}]冲正数量已超出原子订单可退数量
                }

                returnNumber = returnNumber.add(orderDetail.getUnitPrice().multiply(goodsNumber));
                if(StrUtil.isNotBlank(orderDetail.getDiscountType())){
                    flag = false;//有优惠商品
                }

                //插入冲正订单商品
                ServiceOrderDetail saveOrderDetail = new ServiceOrderDetail();
                saveOrderDetail.setId(id);//子订单id
                saveOrderDetail.setOrderId(orderId);//冲正订单id
                saveOrderDetail.setGoodsId(orderDetail.getGoodsId());
                saveOrderDetail.setTotalPrice(detail.getTotalPrice());
                saveOrderDetail.setUnitPrice(orderDetail.getUnitPrice());
                saveOrderDetail.setMiddleType(orderDetail.getMiddleType());
                saveOrderDetail.setLitType(orderDetail.getLitType());
                saveOrderDetail.setNumber(goodsNumber);//记录冲正商品数量
                saveOrderDetail.setGoodsName(orderDetail.getGoodsName());
                saveOrderDetail.setGoodsType(orderDetail.getGoodsType());
                saveOrderDetail.setDiscountType(orderDetail.getDiscountType());
                this.serviceOrderDetailMapper.insert(saveOrderDetail);

                //更新原单商品可退数量
                orderDetail.setReturnableNumber(orderDetail.getReturnableNumber().subtract(goodsNumber));
                this.serviceOrderDetailMapper.updateReturnableNumber(orderDetail);
            }

            //没有优惠商品且上送的冲正积分 与 原单计算的积分不一致
            if(flag){
                if(reverseNumber.compareTo(returnNumber) != 0){
                    throw new BusinessException(getErrorInfo(SysConstant.E02000008));//冲正积分与冲正数量不匹配
                }
            }

            //会员积分账户
            ResInfo planResInfo = this.queryMemberPointsRemote.queryBonusPlanByCustId(autoBonusReversal.getCustId(),SysConstant.BP_PLAN_TYPE_DEFAULT);
            TblBonusPlan bonusPlan = JacksonUtil.toObject(getResJson(planResInfo), TblBonusPlan.class);
            planResInfo = null;
            BigDecimal validBefore = bonusPlan.getValidBonus();//记录操作前的积分数

            //会员需要冲正的积分有效期
            BonusPlanDetailParams bonusPlanDetailParams = new BonusPlanDetailParams();
            bonusPlanDetailParams.setCustId(custId);
            bonusPlanDetailParams.setBpPlanType(SysConstant.BP_PLAN_TYPE_DEFAULT);
            bonusPlanDetailParams.setIsOrderByDate(1);
            ResInfo planDetailsResInfo = this.queryMemberPointsRemote.queryBonusPlanDetail(bonusPlanDetailParams);
            List<TblBonusPlanDetail> bonusPlanDetailList = JacksonUtil.toObject(getResJson(planDetailsResInfo),new TypeReference<List<TblBonusPlanDetail>>(){});
            Map<String, TblBonusPlanDetail> bonusPlanDetailMap = bonusPlanDetailList.stream().collect(Collectors.toMap(TblBonusPlanDetail::getValidDate, Function.identity()));
            bonusPlanDetailParams = null;
            planDetailsResInfo = null;
            bonusPlanDetailList = null;

            int serial = 0;
            BigDecimal surplusBonus = reverseNumber;//剩余冲正积分
            BigDecimal extendValidateNumber = BigDecimal.ZERO;//需要延长有效期的分数
            //按照有效期倒序 先冲有效期最大的
            List<ServiceBonusDetail> returnList = bonusDetailList.stream().sorted(Comparator.comparing(ServiceBonusDetail::getValidDate).reversed()).collect(Collectors.toList());
            for (ServiceBonusDetail bonusDetail : returnList) {
                if(surplusBonus.compareTo(BigDecimal.ZERO) == 0){ //冲正完毕退出
                    break;
                }
                if(bonusDetail.getReturnableNumber().compareTo(BigDecimal.ZERO) == 0){ //流水可退积分为0跳过
                    continue;
                }
                BigDecimal operateBonus;//本次操作积分
                String validDate = bonusDetail.getValidDate();//流水有效期
                BigDecimal bonusDetailNumber = bonusDetail.getReturnableNumber();//流水可退积分
                if(surplusBonus.compareTo(bonusDetailNumber) > 0){ //剩余冲正积分>流水积分
                    operateBonus = bonusDetailNumber;
                }else{
                    operateBonus = surplusBonus;
                }

                Short saveOperate = SysConstant.BONUS_OPERATE_INCREASE;//取反操作
                TblBonusPlanDetail bonusPlanDetail = bonusPlanDetailMap.get(validDate);//根据有效期拿出客户的积分有效期详情
                boolean isOverdue = DateUtil.isBefore4LocalDate(validDate, yearFlag, DateUtil.DATE_FORMAT_COMPACT);//冲正的有效期是否已过期

                if(isOverdue){//当前流水积分有效期已过期,将自动延期到今年
                    bonusPlanDetail.setExpiredStatus(SysConstant.BONUS_PLAN_DETAIL_EXPIRED);
                    bonusPlanDetail.setTotalBonus(bonusPlanDetail.getTotalBonus().subtract(operateBonus));
                    bonusPlanDetail.setApplyBonus(bonusPlanDetail.getApplyBonus().subtract(operateBonus));
                    extendValidateNumber = operateBonus;
                }else{
                    bonusPlanDetail.setApplyBonus(bonusPlanDetail.getApplyBonus().subtract(operateBonus));
                    bonusPlanDetail.setValidBonus(bonusPlanDetail.getValidBonus().add(operateBonus));
                }

                bonusPlan.setApplyBonus(bonusPlan.getApplyBonus().subtract(operateBonus));
                bonusPlan.setValidBonus(bonusPlan.getValidBonus().add(operateBonus));

                //更新客户有效期积分
                bonusPlanDetail.setModifyDate(hostDate);
                bonusPlanDetail.setModifyTime(hostTime);
                this.tblBonusPlanDetailMapper.updatePlanDetailBonus(bonusPlanDetail);

                //插入本次冲正的积分流水
                if(!isOverdue){
                    ServiceBonusDetail serviceBonusDetail = new ServiceBonusDetail();
                    serviceBonusDetail.setOrderId(orderId);
                    serviceBonusDetail.setOrderSerial(serial);
                    serviceBonusDetail.setOperate(saveOperate);
                    serviceBonusDetail.setValidDate(validDate);
                    serviceBonusDetail.setNumber(operateBonus);
                    this.serviceBonusDetailMapper.insert(serviceBonusDetail);
                }

                //更新原订单流水可退积分
                bonusDetail.setReturnableNumber(bonusDetail.getReturnableNumber().subtract(operateBonus));
                this.serviceBonusDetailMapper.updateReturnableNumber(bonusDetail);

                surplusBonus = surplusBonus.subtract(operateBonus);
                serial++;
            }

            //自动延期
            this.autoExtension(extendValidateNumber,bonusPlanDetailMap,yearFlag,hostDate,hostTime,custId,orderId,serial);


            //更新客户账户积分
            bonusPlan.setModifyDate(hostDate);
            bonusPlan.setModifyTime(hostTime);
            this.tblBonusPlanMapper.updatePlanBonus(bonusPlan);

            //更新原订单可退积分
            serviceOrder = new ServiceOrder();
            serviceOrder.setId(autoBonusReversal.getTargetOrderId());
            BigDecimal surplusNumber = order.getReturnableNumber().subtract(reverseNumber);
            if(surplusNumber.compareTo(BigDecimal.ZERO) == 0){
                serviceOrder.setStatus(SysConstant.ORDER_STATUS_REVERSAL_FULL);
            }else{
                serviceOrder.setStatus(SysConstant.ORDER_STATUS_REVERSAL_PART);
            }
            serviceOrder.setReturnableNumber(surplusNumber);
            this.serviceOrderMapper.updateByPrimaryKeySelective(serviceOrder);

            //更新冲正订单为成功
            serviceOrder = new ServiceOrder();
            serviceOrder.setId(orderId);
            serviceOrder.setValidBefore(validBefore);
            serviceOrder.setValidAfter(bonusPlan.getValidBonus());//交易后的分数
            serviceOrder.setNumber(reverseNumber);
            serviceOrder.setReturnableNumber(BigDecimal.ZERO);
            serviceOrder.setStatus(SysConstant.ORDER_STATUS_SUCCESS);
            this.serviceOrderMapper.updateByPrimaryKeySelective(serviceOrder);
        }catch (Exception e){
            throw e;
        }finally {
            rLock.unlock();
        }
    }

    /**
     * 部分冲正-兼容老交易
     * @param autoTblBonusReversal
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public void tblBonusReversalPart(AutoTblBonusReversal autoTblBonusReversal) throws Exception{
        String lockName = SysConstant.LOCK_MEMBER_POINTS + autoTblBonusReversal.getCustId();
        RLock rLock = this.redissonClient.getLock(lockName);
        ServiceOrder serviceOrder;
        try {
            boolean isOk = rLock.tryLock(this.apolloBean.getRedissonTimeWait(), this.apolloBean.getRedissonTimeExpired(), TimeUnit.SECONDS);
            if (!isOk) {
                throw new BusinessException(getErrorInfo(SysConstant.SYS_LOCK_ERROR));//获取锁失败
            }

            ResInfo orderResInfo = this.queryMemberPointsRemote.queryTblOrderByOrderId(autoTblBonusReversal.getTargetOrderId());
            TblOrder order = JacksonUtil.toObject(getResJson(orderResInfo),TblOrder.class);//老原订单
            orderResInfo = null;

            ResInfo txnDetailResInfo = this.queryMemberPointsRemote.queryTxnDetailByAcqSsn(order.getAcqSsn());
            TblTxnDetail txnDetail = JacksonUtil.toObject(getResJson(txnDetailResInfo),TblTxnDetail.class);//原单交易流水
            txnDetailResInfo = null;

            ResInfo tblOrderDetailResInfo = this.queryMemberPointsRemote.queryOrderDetailByOrderId(order.getOrderId());
            List<TblOrderDetail> tblOrderDetailList = JacksonUtil.toObject(getResJson(tblOrderDetailResInfo),new TypeReference<List<TblOrderDetail>>(){});//原单商品
            tblOrderDetailResInfo = null;

            ResInfo tblBonusDetailResInfo = this.queryMemberPointsRemote.queryBonusDetailByBonusSsn(order.getBonusSsn());
            List<TblBonusDetail> tblBonusDetailList = JacksonUtil.toObject(getResJson(tblBonusDetailResInfo),new TypeReference<List<TblBonusDetail>>(){});//原单积分流水
            tblBonusDetailResInfo = null;

            String hostDate = DateUtil.date2String(autoTblBonusReversal.getHostDateTime(), DateUtil.DATE_FORMAT_COMPACT);//主机日期
            String hostTime = DateUtil.date2String(autoTblBonusReversal.getHostDateTime(), DateUtil.DATE_FORMAT_TIME);//主机时间
            String yearFlag = DateUtil.getCurrentDateyyyy() + SysConstant.YEAR_FLAG;//用于判断冲正的有效期是否已过期
            BigDecimal reverseNumber = autoTblBonusReversal.getReverseNumber();//冲正积分
            String orderId = autoTblBonusReversal.getOrderId();//冲正订单id
            String custId = autoTblBonusReversal.getCustId();//会员id

            if(reverseNumber.compareTo(order.getOrderBonus()) > 0){
                throw new BusinessException(getErrorInfo(SysConstant.E02000007));//冲正积分数不可大于原单可冲正积分数
            }

            //原单商品转成map<子订单编号,商品>
            Map<String, TblOrderDetail> orderDetailMap = tblOrderDetailList.stream().collect(Collectors.toMap(TblOrderDetail::getOrderDetailId, Function.identity()));
            TblOrderDetail orderDetailParam;
            for (ReversalDetail detail : autoTblBonusReversal.getDetails()) {//遍历冲正商品
                String id = detail.getId();//冲正子订单号
                BigDecimal goodsNumber = detail.getNumber();//冲正商品数量
                TblOrderDetail tblOrderDetail = orderDetailMap.get(id);

                if(tblOrderDetail == null){
                    continue;
                    //throw new BusinessException(getErrorInfo(SysConstant.E02000011),new Object[]{id});//原订单中无{0}子订单号
                }

                //校验可退数量
                if(goodsNumber.compareTo(tblOrderDetail.getOrderNum()) > 0){
                    throw new BusinessException(getErrorInfo(SysConstant.E02000010),new Object[]{id});//子订单号[{0}]冲正数量已超出原子订单可退数量
                }

                //保存冲正订单商品
                ServiceOrderDetail saveOrderDetail = new ServiceOrderDetail();
                saveOrderDetail.setOrderId(orderId);//冲正订单id
                saveOrderDetail.setId(id);//冲正子订单id
                saveOrderDetail.setGoodsId(tblOrderDetail.getGoodsId());//冲正商品id
                saveOrderDetail.setTotalPrice(detail.getTotalPrice());
                saveOrderDetail.setUnitPrice(BigDecimal.ZERO);//老交易没有商品单价
                saveOrderDetail.setNumber(goodsNumber);//冲正商品数量
                saveOrderDetail.setReturnableNumber(goodsNumber);//冲正商品数量
                saveOrderDetail.setGoodsName(tblOrderDetail.getGoodsNm());//冲正商品数量
                saveOrderDetail.setGoodsType(SysConstant.GOODS_TYPE_NOT_OIL);//非油品
                this.serviceOrderDetailMapper.insert(saveOrderDetail);

                //更新原单商品可退数量，商品状态，冲正时间
                orderDetailParam = new TblOrderDetail();
                orderDetailParam.setPkOrderDtl(tblOrderDetail.getPkOrderDtl());
                orderDetailParam.setOrderCancel("0");
                orderDetailParam.setReturnTime(hostDate);
                this.tblOrderDetailMapper.updateByPrimaryKeySelective(orderDetailParam);
            }
            orderDetailParam = null;

            //会员积分账户
            ResInfo planResInfo = this.queryMemberPointsRemote.queryBonusPlanByCustId(custId,SysConstant.BP_PLAN_TYPE_DEFAULT);
            TblBonusPlan bonusPlan = JacksonUtil.toObject(getResJson(planResInfo), TblBonusPlan.class);
            planResInfo = null;
            serviceOrder = new ServiceOrder();
            serviceOrder.setValidBefore(bonusPlan.getValidBonus());//记录交易前总账积分

            //会员需要冲正的积分有效期
            BonusPlanDetailParams bonusPlanDetailParams = new BonusPlanDetailParams();
            bonusPlanDetailParams.setCustId(custId);//会员id
            bonusPlanDetailParams.setBpPlanType(SysConstant.BP_PLAN_TYPE_DEFAULT);//积分计划
            bonusPlanDetailParams.setIsOrderByDate(1);//按有效期倒序
            ResInfo planDetailsResInfo = this.queryMemberPointsRemote.queryBonusPlanDetail(bonusPlanDetailParams);
            List<TblBonusPlanDetail> bonusPlanDetailList = JacksonUtil.toObject(getResJson(planDetailsResInfo),new TypeReference<List<TblBonusPlanDetail>>(){});
            Map<String, TblBonusPlanDetail> bonusPlanDetailMap = bonusPlanDetailList.stream().collect(Collectors.toMap(TblBonusPlanDetail::getValidDate, Function.identity()));
            bonusPlanDetailParams = null;
            planDetailsResInfo = null;
            bonusPlanDetailList = null;

            int serial = 0;
            BigDecimal surplusBonus = reverseNumber;//剩余冲正积分
            BigDecimal extendValidateNumber = BigDecimal.ZERO;//需要延长有效期的分数
            TblBonusDetail tblBonusDetailParam;
            Short operate = null;
            for (TblBonusDetail tblBonusDetail : tblBonusDetailList) { //遍历原单积分流水
                if(surplusBonus.compareTo(BigDecimal.ZERO) == 0){ //冲正完毕退出
                    break;
                }
                String validDate = tblBonusDetail.getValidDate();//原流水有效期
                String bonusCdFlag = tblBonusDetail.getBonusCdFlag();//原流水操作
                BigDecimal bonusDetailNumber = tblBonusDetail.getTxnBonus();//原流水可退积分

                TblBonusPlanDetail planDetail = bonusPlanDetailMap.get(validDate);//取出客户有效期账户
                operate = "c".equals(bonusCdFlag) ? SysConstant.BONUS_OPERATE_INCREASE : SysConstant.BONUS_OPERATE_DECREASE;//取反操作
                boolean isOverdue = DateUtil.isBefore4LocalDate(validDate, yearFlag, DateUtil.DATE_FORMAT_COMPACT);//冲正的有效期是否已过期
                boolean isExtend = false;//是否有自动延长有效期操作

                BigDecimal operateBonus;//本次操作积分
                if(surplusBonus.compareTo(bonusDetailNumber) > 0){
                    operateBonus = bonusDetailNumber; //剩余冲正积分>原流水可冲正积分，那么本次就冲完原流水可冲正积分
                }else{
                    operateBonus = surplusBonus;
                }
                if(SysConstant.BONUS_OPERATE_INCREASE.equals(operate)){
                    if(isOverdue){
                        //自动延期到今年
                        isExtend = true;
                        planDetail.setExpiredStatus(SysConstant.BONUS_PLAN_DETAIL_EXPIRED);
                        planDetail.setTotalBonus(planDetail.getTotalBonus().subtract(operateBonus));
                        planDetail.setApplyBonus(planDetail.getApplyBonus().subtract(operateBonus));
                        extendValidateNumber = extendValidateNumber.add(operateBonus);
                    }else{
                        planDetail.setValidBonus(planDetail.getValidBonus().add(operateBonus));
                        planDetail.setApplyBonus(planDetail.getApplyBonus().subtract(operateBonus));
                    }
                    bonusPlan.setValidBonus(bonusPlan.getValidBonus().add(operateBonus));
                    bonusPlan.setApplyBonus(bonusPlan.getApplyBonus().subtract(operateBonus));
                }else{
                    planDetail.setTotalBonus(planDetail.getTotalBonus().subtract(operateBonus));
                    planDetail.setValidBonus(planDetail.getValidBonus().subtract(operateBonus));

                    bonusPlan.setTotalBonus(bonusPlan.getTotalBonus().subtract(operateBonus));
                    bonusPlan.setValidBonus(bonusPlan.getValidBonus().subtract(operateBonus));
                }
                //更新客户有效期积分
                planDetail.setModifyDate(hostDate);
                planDetail.setModifyTime(hostTime);
                this.tblBonusPlanDetailMapper.updatePlanDetailBonus(planDetail);

                if(!isExtend){
                    //没有自动延期的才会插流水（延期的会另插）
                    ServiceBonusDetail serviceBonusDetail = new ServiceBonusDetail();
                    serviceBonusDetail.setOrderId(autoTblBonusReversal.getOrderId());
                    serviceBonusDetail.setOrderSerial(serial);
                    serviceBonusDetail.setOperate(operate);
                    serviceBonusDetail.setValidDate(validDate);
                    serviceBonusDetail.setNumber(operateBonus);
                    serviceBonusDetail.setReturnableNumber(operateBonus);
                    this.serviceBonusDetailMapper.insert(serviceBonusDetail);
                }

                //更新原流水可退积分
                tblBonusDetailParam = new TblBonusDetail();
                tblBonusDetailParam.setPkBonusDetail(tblBonusDetail.getPkBonusDetail());
                tblBonusDetailParam.setReturnFlag("1");
                this.tblBonusDetailMapper.updateByPrimaryKeySelective(tblBonusDetailParam);

                surplusBonus = surplusBonus.subtract(operateBonus);
                serial++;
            }
            tblBonusDetailParam = null;

            //更新原单
            order.setOrderStatus("01");
            order.setReturnTime(hostDate);
            this.tblOrderMapper.updateStatus(order);

            //更新原单交易流水
            txnDetail.setReturnFlag("1");
            this.tblTxnDetailMapper.updateReturnFlag(txnDetail);

            //自动延期
            this.autoExtension(extendValidateNumber,bonusPlanDetailMap,yearFlag,hostDate,hostTime,custId,orderId,serial);

            //更新客户账户积分
            bonusPlan.setModifyDate(hostDate);
            bonusPlan.setModifyTime(hostTime);
            this.tblBonusPlanMapper.updatePlanBonus(bonusPlan);

            //更新冲正订单为成功
            serviceOrder.setValidAfter(bonusPlan.getValidBonus());//交易后的分数
            serviceOrder.setNumber(reverseNumber);
            serviceOrder.setReturnableNumber(BigDecimal.ZERO);
            serviceOrder.setId(orderId);
            serviceOrder.setOperate(operate);
            serviceOrder.setStatus(SysConstant.ORDER_STATUS_SUCCESS);
            this.serviceOrderMapper.updateByPrimaryKeySelective(serviceOrder);
        }catch (Exception e){
            throw e;
        }finally {
            rLock.unlock();
        }
    }

    /**
     * 自动延期冲正积分
     * @param extendValidateNumber
     * @param bonusPlanDetailMap
     * @param yearFlag
     * @param hostDate
     * @param hostTime
     * @param custId
     * @param orderId
     * @param serial
     */
    private void autoExtension(BigDecimal extendValidateNumber,Map<String, TblBonusPlanDetail> bonusPlanDetailMap,String yearFlag,String hostDate,String hostTime,String custId,String orderId,int serial){
        if(extendValidateNumber.compareTo(BigDecimal.ZERO) > 0){
            log.info("客户[{}]自动延长积分有效期到[{}],积分数[{}]",custId,yearFlag,extendValidateNumber);
            TblBonusPlanDetail tblBonusPlanDetail = bonusPlanDetailMap.get(yearFlag);
            if(tblBonusPlanDetail != null){
                tblBonusPlanDetail.setTotalBonus(tblBonusPlanDetail.getTotalBonus().add(extendValidateNumber));
                tblBonusPlanDetail.setValidBonus(tblBonusPlanDetail.getValidBonus().add(extendValidateNumber));
                tblBonusPlanDetail.setModifyDate(hostDate);
                tblBonusPlanDetail.setModifyTime(hostTime);
                this.tblBonusPlanDetailMapper.updatePlanDetailBonus(tblBonusPlanDetail);
            }else{
                log.info("客户[{}]没有该积分有效期[{}],将自动创建",custId,yearFlag);
                TblBonusPlanDetail detail = new TblBonusPlanDetail();
                detail.setUsageKey(SysConstant.DEFAULT_USAGE_KEY);
                detail.setCustId(custId);
                detail.setBpPlanType(SysConstant.BP_PLAN_TYPE_DEFAULT);
                detail.setTotalBonus(extendValidateNumber);
                detail.setValidBonus(extendValidateNumber);
                detail.setApplyBonus(BigDecimal.ZERO);
                detail.setExpireBonus(BigDecimal.ZERO);
                detail.setValidDate(yearFlag);
                detail.setExpiredStatus(SysConstant.BONUS_PLAN_DETAIL_IN_EFFECT);
                detail.setCreateOper(SysConstant.DEFAULT_CREATE_OPER);
                detail.setCreateDate(hostDate);
                detail.setCreateTime(hostTime);
                detail.setModifyOper(SysConstant.DEFAULT_CREATE_OPER);
                detail.setModifyDate(hostDate);
                detail.setModifyTime(hostTime);
                this.tblBonusPlanDetailMapper.insert(detail);
            }
            //插入本次冲正的积分流水
            ServiceBonusDetail serviceBonusDetail = new ServiceBonusDetail();
            serviceBonusDetail.setOrderId(orderId);//冲正订单的id
            serviceBonusDetail.setOrderSerial(serial);
            serviceBonusDetail.setOperate(SysConstant.BONUS_OPERATE_INCREASE);
            serviceBonusDetail.setValidDate(yearFlag);
            serviceBonusDetail.setNumber(extendValidateNumber);
            this.serviceBonusDetailMapper.insert(serviceBonusDetail);
        }
    }

    /**
     * 积分调整
     * @param adjustOrder
     */
    @Transactional(rollbackFor = Exception.class)
    public SendWeChat excuteAdjust(AdjustOrder adjustOrder) throws Exception{
        String lockName = SysConstant.LOCK_MEMBER_POINTS + adjustOrder.getCustId();
        RLock rLock = this.redissonClient.getLock(lockName);
        SendWeChat sendWeChat = new SendWeChat();
        try {
            boolean isOk = rLock.tryLock(this.apolloBean.getRedissonTimeWait(), this.apolloBean.getRedissonTimeExpired(), TimeUnit.SECONDS);
            if (!isOk) {
                throw new BusinessException(getErrorInfo(SysConstant.SYS_LOCK_ERROR));//获取锁失败
            }

            String validate = DateUtil.getCurrentDateyyyy() + SysConstant.YEAR_FLAG; //调整的有效期
            Date hostDate = adjustOrder.getHostDate();
            String hostDateStr = DateUtil.date2String(hostDate,DateUtil.DATE_FORMAT_COMPACT);//主机日期
            String hostTimeStr = DateUtil.date2String(hostDate,DateUtil.DATE_FORMAT_TIME);//主机时间

            /** 客户积分总账 **/
            ResInfo planResInfo = queryMemberPointsRemote.queryBonusPlanByCustId(adjustOrder.getCustId(), SysConstant.BP_PLAN_TYPE_DEFAULT);
            TblBonusPlan bonusPlan = JacksonUtil.toObject(getResJson(planResInfo), TblBonusPlan.class);
            if(bonusPlan == null){
                log.info("客户[{}]没有对应的积分计划[{}]的TblBonusPlan数据，自动创建",adjustOrder.getCustId(),SysConstant.DEFAULT_USAGE_KEY);
                bonusPlan = new TblBonusPlan();
                bonusPlan.setUsageKey(SysConstant.DEFAULT_USAGE_KEY);
                bonusPlan.setCustId(adjustOrder.getCustId());
                bonusPlan.setLockStatus(SysConstant.BONUS_PLAN_LOCK_STATUS_NORMAL);//账户锁定状态
                bonusPlan.setBpPlanType(SysConstant.BP_PLAN_TYPE_DEFAULT);//积分计划
                bonusPlan.setTotalBonus(BigDecimal.ZERO);//总有效积分
                bonusPlan.setValidBonus(BigDecimal.ZERO);//有效积分
                bonusPlan.setApplyBonus(BigDecimal.ZERO);//已用积分
                bonusPlan.setExpireBonus(BigDecimal.ZERO);//失效积分
            }
            planResInfo = null;

            /** 客户积分有效期 **/
            TblBonusPlanDetail currBonusPlanDetail = null;

            List<String> queryValidates = new ArrayList<>();
            queryValidates.add(validate);
            BonusPlanDetailParams bonusPlanDetailParams = new BonusPlanDetailParams();
            bonusPlanDetailParams.setCustId(adjustOrder.getCustId());
            bonusPlanDetailParams.setBpPlanType(SysConstant.BP_PLAN_TYPE_DEFAULT);
            bonusPlanDetailParams.setValidates(queryValidates);
            ResInfo planDetailsResInfo = this.queryMemberPointsRemote.queryBonusPlanDetail(bonusPlanDetailParams);
            List<TblBonusPlanDetail> bonusPlanDetailList = JacksonUtil.toObject(getResJson(planDetailsResInfo),new TypeReference<List<TblBonusPlanDetail>>(){});
            if(bonusPlanDetailList != null && bonusPlanDetailList.size() > 0){
                currBonusPlanDetail = bonusPlanDetailList.get(0);
            }else{
                log.info("客户[{}]没有对应的积分计划[{}]有效期为[{}]的tblBonusPlanDetail数据，自动创建",adjustOrder.getCustId(),SysConstant.BP_PLAN_TYPE_DEFAULT,validate);
                currBonusPlanDetail = new TblBonusPlanDetail();
                currBonusPlanDetail.setUsageKey(SysConstant.DEFAULT_USAGE_KEY);
                currBonusPlanDetail.setCustId(adjustOrder.getCustId());
                currBonusPlanDetail.setBpPlanType(SysConstant.BP_PLAN_TYPE_DEFAULT);
                currBonusPlanDetail.setTotalBonus(BigDecimal.ZERO);
                currBonusPlanDetail.setValidBonus(BigDecimal.ZERO);
                currBonusPlanDetail.setApplyBonus(BigDecimal.ZERO);
                currBonusPlanDetail.setExpireBonus(BigDecimal.ZERO);
                currBonusPlanDetail.setValidDate(validate);
                currBonusPlanDetail.setExpiredStatus(SysConstant.BONUS_PLAN_DETAIL_IN_EFFECT);
                currBonusPlanDetail.setLockStatus(SysConstant.BONUS_PLAN_DETAIL_LOCK_STATUS_NORMAL);
            }
            queryValidates = null;
            bonusPlanDetailParams = null;
            planDetailsResInfo = null;
            bonusPlanDetailList = null;

            Short operate = SysConstant.BONUS_OPERATE_INCREASE;//执行操作
            BigDecimal number = adjustOrder.getNumber();//操作分数
            String adjustType = SysConstant.BONUS_FLAG_D;//调整方式

            /** 调整账户分数 **/
            ServiceOrder serviceOrderDto = new ServiceOrder();
            serviceOrderDto.setId(adjustOrder.getServiceOrderId());
            serviceOrderDto.setValidBefore(bonusPlan.getValidBonus());//操作前分数
            this.operatePlanAndDetail(bonusPlan,currBonusPlanDetail,operate,number);
            serviceOrderDto.setValidAfter(bonusPlan.getValidBonus());//操作后分数
            serviceOrderDto.setStatus(SysConstant.ORDER_STATUS_SUCCESS);

            /** 更新有效期信息 **/
            currBonusPlanDetail.setModifyOper(adjustOrder.getCustId());
            currBonusPlanDetail.setModifyDate(hostDateStr);
            currBonusPlanDetail.setModifyTime(hostTimeStr);
            if(currBonusPlanDetail.getPkBonusPlanDetail() == null){
                currBonusPlanDetail.setCreateOper(SysConstant.DEFAULT_CREATE_OPER);
                currBonusPlanDetail.setCreateDate(hostDateStr);
                currBonusPlanDetail.setCreateTime(hostTimeStr);
                this.tblBonusPlanDetailMapper.insert(currBonusPlanDetail);
            }else{
                this.tblBonusPlanDetailMapper.updatePlanDetailBonus(currBonusPlanDetail);
            }

            /** 保存CheckInf信息 **/
            TblCheckInf checkInf = new TblCheckInf();
            checkInf.setTeller1(SysConstant.DEFAULT_CREATE_OPER);//提交人
            checkInf.setTeller2(SysConstant.DEFAULT_CREATE_OPER);//审核人
            checkInf.setBpPlanType(SysConstant.BP_PLAN_TYPE_DEFAULT);
            checkInf.setCustId(adjustOrder.getCustId());
            checkInf.setAdjustType(adjustType);//调整方式
            checkInf.setValidDate(validate);
            checkInf.setTxnBonus(number.longValue());//调整分数
            checkInf.setTxnDesc(adjustOrder.getTxnDesc());//调整原因
            checkInf.setOprFlag("1");//审核状态
            checkInf.setCreateOper(SysConstant.DEFAULT_CREATE_OPER);//创建人
            checkInf.setCreateDate(hostDateStr);
            checkInf.setCreateTime(hostTimeStr);
            checkInf.setModifyOper(SysConstant.DEFAULT_CREATE_OPER);
            checkInf.setModifyDate(DateUtil.date2String(adjustOrder.getBusinessDate(),DateUtil.DATE_FORMAT_COMPACT));//存入营业日
            checkInf.setModifyTime(hostTimeStr);
            checkInf.setBussId(currBonusPlanDetail.getPkBonusPlanDetail().toString());//有效期主键
            checkInf.setBussType("02");//积分调整
            checkInf.setTxnItems(adjustOrder.getTxnItems());//调整事项
            checkInf.setStationId(adjustOrder.getStationId());
            checkInf.setBonusIntoType("0");//实时到账
            checkInf.setAdjustProperty(adjustOrder.getAdjustProperty());//调整性质
            checkInf.setAdjustOrderId(adjustOrder.getMallId());
            this.tblCheckInfMapper.insertSelective(checkInf);

            /** 把调整原因当成商品名 **/
            ServiceOrderDetail delayOrderDetail = new ServiceOrderDetail();
            delayOrderDetail.setId(checkInf.getTblCheckInf().toString());//与调整表关联
            delayOrderDetail.setOrderId(adjustOrder.getServiceOrderId());
            delayOrderDetail.setGoodsId(String.valueOf(adjustOrder.getServiceOrderId()));//把订单id当成商品id
            delayOrderDetail.setTotalPrice(number);
            delayOrderDetail.setUnitPrice(number);
            delayOrderDetail.setNumber(BigDecimal.ONE);
            delayOrderDetail.setReturnableNumber(BigDecimal.ONE);
            delayOrderDetail.setGoodsName(adjustOrder.getTxnDesc());
            delayOrderDetail.setGoodsType(SysConstant.GOODS_TYPE_REISSUE);
            this.serviceOrderDetailMapper.insert(delayOrderDetail);

            /** 保存积分流水信息 **/
            ServiceBonusDetail newServiceBonusDetail = new ServiceBonusDetail();
            newServiceBonusDetail.setOrderId(adjustOrder.getServiceOrderId());
            newServiceBonusDetail.setOrderSerial(0);
            newServiceBonusDetail.setOperate(operate);
            newServiceBonusDetail.setValidDate(validate);
            newServiceBonusDetail.setNumber(number);
            newServiceBonusDetail.setReturnableNumber(number);
            this.serviceBonusDetailMapper.insert(newServiceBonusDetail);

            /*更新订单信息*/
            this.serviceOrderMapper.updateByPrimaryKeySelective(serviceOrderDto);

            /** 更新账户信息 **/
            bonusPlan.setModifyOper(adjustOrder.getCustId());
            bonusPlan.setModifyDate(hostDateStr);
            bonusPlan.setModifyTime(hostTimeStr);
            if(bonusPlan.getPkBonusPlan() == null){
                bonusPlan.setCreateOper(SysConstant.DEFAULT_CREATE_OPER);
                bonusPlan.setCreateDate(hostDateStr);
                bonusPlan.setCreateTime(hostTimeStr);
                this.tblBonusPlanMapper.insert(bonusPlan);
            }else{
                this.tblBonusPlanMapper.updatePlanBonus(bonusPlan);
            }

            /** 发送推送 **/
//            CompletableFuture.runAsync(() -> {
//                SendWeChat sendWeChat = new SendWeChat();
//                sendWeChat.setTitle(SysConstant.MEMBER_POINTS_MSG_TITLE);
//                sendWeChat.setOpenId(adjustOrder.getOpenId());
//                sendWeChat.setCardNo(adjustOrder.getCustId());
//                sendWeChat.setCentent(adjustOrder.getTxnDesc());
//                sendWeChat.setCode(adjustOrder.getStationId());
//                try {
//                    this.memberPointsRelateService.sendWeChatMsg(sendWeChat, SysConstant.SEND_WECHAT_MSG_EXTERNAL_ADJUST);
//                } catch (Exception e) {
//                    log.error("积分赠送推送异常：[{}]",e.getMessage());
//                }
//            });

                sendWeChat.setTitle(SysConstant.MEMBER_POINTS_MSG_TITLE);
                sendWeChat.setOpenId(adjustOrder.getOpenId());
                sendWeChat.setCardNo(adjustOrder.getCustId());
                sendWeChat.setCentent(adjustOrder.getTxnDesc());
                sendWeChat.setCode(adjustOrder.getStationId());

                return sendWeChat;
        }catch (Exception e){
            throw e;
        }finally {
            rLock.unlock();
        }
    }
}
