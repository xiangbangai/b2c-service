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
     * ??????????????????
     * @param id
     * @param custId
     * @param exchangeGoods
     * @param totalPrice
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public ExchangeResult exchange(String id, String custId, Date date, List<ExchangeGoods> exchangeGoods, BigDecimal totalPrice) throws Exception{
        /** ???????????? **/
        ExchangeResult result = new ExchangeResult();
        result.setTotalConsume(totalPrice);
        BigDecimal expBonus = BigDecimal.ZERO;//?????????????????????????????????
        String expDate = DateUtil.date2String(date, DateUtil.DATE_YEAR) + SysConstant.YEAR_FLAG;//????????????


        /**
         * ????????????????????????
         * ?????????????????????????????????????????????????????????????????????exchange
         */
        String lockName = SysConstant.LOCK_MEMBER_POINTS + custId;
        RLock rLock = this.redissonClient.getLock(lockName);
        try {
            boolean isOk = rLock.tryLock(this.apolloBean.getRedissonTimeWait(), this.apolloBean.getRedissonTimeExpired(), TimeUnit.SECONDS);
            //???????????????
            if (!isOk) {
                throw new BusinessException(getErrorInfo(SysConstant.SYS_LOCK_ERROR));//???????????????
            }

            //????????????
            ResInfo bonusPlanResInfo = queryMemberPointsRemote.queryBonusPlanByCustId(custId, SysConstant.BP_PLAN_TYPE_DEFAULT);
            TblBonusPlan tblBonusPlan = JacksonUtil.toObject(getResJson(bonusPlanResInfo), TblBonusPlan.class);

            if (tblBonusPlan == null || tblBonusPlan.getValidBonus().compareTo(totalPrice) == -1) {
                throw new BusinessException(getErrorInfo(SysConstant.E02000004)); //??????????????????
            }
            if (SysConstant.BP_LOCK_STATUS_LOCKED.equals(tblBonusPlan.getLockStatus())) {
                throw new BusinessException(getErrorInfo(SysConstant.E02000005)); //?????????????????????
            }

            BigDecimal validBonus = NumberUtil.null2Zero(tblBonusPlan.getValidBonus());//??????????????????
            BigDecimal applyBonus = NumberUtil.null2Zero(tblBonusPlan.getApplyBonus());//??????????????????
            tblBonusPlan.setValidBonus(validBonus.subtract(totalPrice));
            tblBonusPlan.setApplyBonus(applyBonus.add(totalPrice));
            tblBonusPlan.setModifyOper(custId);
            tblBonusPlan.setModifyDate(DateUtil.date2String(date, DateUtil.DATE_FORMAT_COMPACT));
            tblBonusPlan.setModifyTime(DateUtil.date2String(date, DateUtil.DATE_FORMAT_TIME));
            this.tblBonusPlanMapper.updatePlanBonus(tblBonusPlan);

            /**????????????**/
            ServiceOrder serviceOrder = new ServiceOrder();
            serviceOrder.setId(id);
            serviceOrder.setNumber(totalPrice);
            serviceOrder.setReturnableNumber(totalPrice);
            serviceOrder.setValidBefore(validBonus);
            serviceOrder.setValidAfter(validBonus.subtract(totalPrice));
            serviceOrder.setStatus(SysConstant.ORDER_STATUS_SUCCESS);
            serviceOrderMapper.updateByPrimaryKeySelective(serviceOrder);

            /**????????????**/
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

            //????????????
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
                    if (totalPrice.compareTo(BigDecimal.ZERO) == 1) {//???????????????
                        resultTemp = tblBonusPlanDetail.getValidBonus().subtract(totalPrice);

                        if (resultTemp.compareTo(BigDecimal.ZERO) == -1) { //?????????????????????
                            thisTemp = totalPrice.subtract(resultTemp.abs());//??????????????????
                            totalPrice = resultTemp.abs(); //????????????
                            tblBonusPlanDetail.setValidBonus(BigDecimal.ZERO); //??????????????????
                        } else {
                            thisTemp = totalPrice; //??????????????????
                            totalPrice = BigDecimal.ZERO; //????????????
                            tblBonusPlanDetail.setValidBonus(resultTemp); //??????????????????
                        }


                        tblBonusPlanDetail.setApplyBonus(tblBonusPlanDetail.getApplyBonus().add(thisTemp));
                        tblBonusPlanDetail.setModifyOper(custId);
                        tblBonusPlanDetail.setModifyDate(DateUtil.date2String(date, DateUtil.DATE_FORMAT_COMPACT));
                        tblBonusPlanDetail.setModifyTime(DateUtil.date2String(date, DateUtil.DATE_FORMAT_TIME));
                        this.tblBonusPlanDetailMapper.updatePlanDetailBonus(tblBonusPlanDetail);

                        serviceBonusDetail = new ServiceBonusDetail();
                        serviceBonusDetail.setOrderId(id);//????????????
                        serviceBonusDetail.setOrderSerial(orderSerial);//??????
                        serviceBonusDetail.setOperate(SysConstant.BONUS_OPERATE_DECREASE);//??????????????????
                        serviceBonusDetail.setValidDate(tblBonusPlanDetail.getValidDate());//???????????????
                        serviceBonusDetail.setNumber(thisTemp);//????????????
                        serviceBonusDetail.setRuleId("");//????????????
                        serviceBonusDetail.setReturnableNumber(thisTemp); //??????

                        serviceBonusDetailMapper.insert(serviceBonusDetail);

                        orderSerial++;
                    }
                    if (expDate.equals(tblBonusPlanDetail.getValidDate())) {
                        expBonus = tblBonusPlanDetail.getValidBonus();
                    }
                }
            }

            /**?????????????????????**/
            if (totalPrice.compareTo(BigDecimal.ZERO) == 1) {
                throw new BusinessException(getErrorInfo(SysConstant.E02000002));//???????????????????????????
            }

            result.setTotalBonus(tblBonusPlan.getValidBonus());//?????????????????????
            result.setExpBonus(NumberUtil.null2Zero(expBonus));//?????????????????????
            result.setExpDate(expDate);//???????????????????????????
        } catch (Exception e) {
            throw e;
        } finally {
            rLock.unlock();
        }
        return result;
    }

    /**
     * ????????????
     * @return
     * @throws Exception
     */
    public List<BonusAccItf> excuteComputeBonus(ComputeBonus computeBonus)throws Exception{
        List<BonusAccItf> resultList = new ArrayList<>(); //????????????

        String channel = computeBonus.getChannel();

        //???????????? ???????????? ????????????
        String txnyyyyMMdd = DateUtil.date2String(computeBonus.getReqDateTime(), DateUtil.DATE_FORMAT_COMPACT);
        String txnHHmmss = DateUtil.date2String(computeBonus.getReqDateTime(), DateUtil.DATE_FORMAT_TIME);
        String businessDate = DateUtil.date2String(computeBonus.getBusinessDate(), DateUtil.DATE_FORMAT_COMPACT);

        //???????????????bin
        Object cardBinObj = getRuleInfo().get(SysConstant.DICT_KEY_RULE_CARD_BIN);
        List<String> diyCadBinValue = cardBinObj == null ? null : (List<String>)cardBinObj;

        //????????????bin????????????
        Map<String, ServiceDict> diyCardBinRange = getServiceDict().get(SysConstant.DICT_KEY_1005000);
        ServiceDict serviceDict = diyCardBinRange.get(SysConstant.DICT_KEY_RULE_CARD_BIN_RANGE);
        String[] diyCardBin = serviceDict.getDictValue().split(",");
        int cardBinStart = Integer.parseInt(diyCardBin[0]);//????????????bin????????????
        int cardBinEnd = Integer.parseInt(diyCardBin[1]);//????????????bin????????????

        //????????????
        Map<String, Double> levelCoefficient = apolloYML.getUserLevel();
        //?????????????????????????????????
        Map<Integer, ServiceNotProduceMidtype> notProduceMidtypeMap = this.getNotProduceMidtypeMap();
        List<Goods> goods = this.analysisNoPointsList(computeBonus.getGoods(), notProduceMidtypeMap);
        computeBonus.setGoods(goods);

        //????????????
        ResInfo stationResInfo = queryStationRemote.queryStationInfo(computeBonus.getStationId());
        Station station = JacksonUtil.toObject(getResJson(stationResInfo),Station.class);

        //???????????? ??????????????????
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

        //?????????????????????????????????
        boolean isWhite = false;
        if(StrUtil.isNotBlank(unionPayCardNo)){
            ResInfo whiteListResInfo = this.queryLimitRemote.queryCustUnionPayWhiteList(unionPayCardNo);
            if(Integer.parseInt(whiteListResInfo.getData()) > 0){
                isWhite = true;
            }
        }

        //????????????
        LabelParam labelParam = new LabelParam();
        labelParam.setCustId(computeBonus.getCustInf().getCustId());//??????id
        labelParam.setCurDate(computeBonus.getReqDateTime());//????????????
        ResInfo resInfo = this.queryCustRemote.queryCustLabel(labelParam);
        List<String> custLabelList = JacksonUtil.toObject(getResJson(resInfo),new TypeReference<List<String>>(){});
        List<String> labelList1 = dealLabelRepat(custLabelList);
        custLabelList = null;
        labelParam = null;

        //????????????
        String birthday = "";
        ResInfo custUserResInfo = this.queryCustRemote.queryCustUser(computeBonus.getCustInf().getCustId());
        TblCustUser tblCustUser = JacksonUtil.toObject(getResJson(custUserResInfo),TblCustUser.class);
        if(tblCustUser != null){
            if(StrUtil.isNotBlank(tblCustUser.getBrithDate()) && (tblCustUser.getBrithDate().length() == 8)){
                birthday = tblCustUser.getBrithDate();
            }
        }


        List<TxnBonus> txnBonusList = new ArrayList<>();
        if(CollUtil.isEmpty(labelList1)){//??????????????????
            this.genTxnBonusList(channel,txnBonusList,txnyyyyMMdd,txnHHmmss,businessDate,diyCadBinValue,cardBinStart,cardBinEnd,levelCoefficient,station,payTypeSet,unionPayCardNo,isWhite,computeBonus,birthday,"");
        }else{//???????????????
            for (String label : labelList1) {
                this.genTxnBonusList(channel,txnBonusList,txnyyyyMMdd,txnHHmmss,businessDate,diyCadBinValue,cardBinStart,cardBinEnd,levelCoefficient,station,payTypeSet,unionPayCardNo,isWhite,computeBonus,birthday,label);
            }
        }

        if(txnBonusList != null){
            TopRules topRules = rulePkgWorker.getRuleFile();
            topRules.execPkg(txnBonusList, ruleFileName);
            //??????????????????
            List<BonusAccItf> bonusAccItfList = getResultFromTxnBonus(txnBonusList);
            Map<String,String> checkMap = new HashMap<>();
            for (BonusAccItf bonusAccItf : bonusAccItfList) {
                if (checkMap.get(bonusAccItf.getRuleId() + bonusAccItf.getGoodsId()) == null) {
                    resultList.add(bonusAccItf);
                    checkMap.put(bonusAccItf.getRuleId() + bonusAccItf.getGoodsId(), bonusAccItf.getRuleId());
                }
            }
        }

        //???????????????????????????????????????
        if(resultList.size() == 0){
            log.info("???????????????????????????");
            for (TxnBonus txnBonus : txnBonusList) {
                log.info("????????????[{}],????????????[{}]",computeBonus.getReqSerialNo(),txnBonus);
            }
        }

        return resultList;
    }

    //?????????????????????????????????
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
     * ????????????????????????
     * @param labelList
     * @return
     */
    private List<String> dealLabelRepat(List<String> labelList) {
        if(CollUtil.isEmpty(labelList)) {
            return labelList;
        }
        boolean bonusSpeed = false;//???????????????????????????????????????
        boolean crm = false;//???????????????CRM????????????
        boolean bjkhy = false;//??????????????????????????????
        boolean zkhy = false;//???????????????????????????
        int bonusSpeedIndex = 0;//?????????????????????????????????
        int bjkhyIndex = 0;//?????????????????????????????????
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
     * ????????????????????????
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
        Map<String, TxnBonus> tempTxnBonusMap = new HashMap<>();//???????????????????????????????????????????????????????????????????????????????????????

        //????????????
        String stationShortName = "";
        String region = "";
        if(station != null){
            stationShortName = station.getShortName();
            region = station.getMarketRegionId().toString();
        }
        //????????????
        TblCustInf custInf = computeBonus.getCustInf();

        for (Goods goods : computeBonus.getGoods()) {
            Short goodsType = goods.getGoodsType();//???????????????0-????????? 1-?????? 2-?????????????????????
            String goodsId = goods.getGoodsId();//????????????
            String goodsName = goods.getGoodsName();//????????????
            String middleType = String.valueOf(goods.getMiddleType());//????????????
            String litType = String.valueOf(goods.getLitType());//????????????
            double totalPrice = goods.getTotalPrice().doubleValue();//??????????????????
            double unitPrice = goods.getUnitPrice().doubleValue();//????????????
            double goodsNumber = goods.getNumber().doubleValue();//??????????????????

            TxnBonus txnBonus = null;
            if(SysConstant.GOODS_TYPE_NOT_OIL.equals(goodsType)){ //?????????
                txnBonus = tempTxnBonusMap.get(SysConstant.NOT_OIL_FLAG);
            }else{ //??????
                txnBonus = tempTxnBonusMap.get(middleType);
            }

            if(txnBonus != null){
                txnBonus.setTxn_amt(txnBonus.getTxn_amt()+ totalPrice);
                txnBonus.setGoods_num(txnBonus.getGoods_num() + goodsNumber);
                txnBonus.setGoods_count(txnBonus.getGoods_count() + goodsNumber);
                txnBonus.setGoods_act_price(txnBonus.getGoods_act_price()+ totalPrice);
            }else{
                txnBonus = new TxnBonus();
                txnBonus.setTxn_date(txnyyyyMMdd);//????????????
                txnBonus.setTxn_time(txnHHmmss);// ????????????
                txnBonus.setCust_id(custInf.getCustId());//?????????
                txnBonus.setAcct_id(computeBonus.getAcctId());//??????
                txnBonus.setAcct_type(null);//???????????? ?????????
                txnBonus.setStation_id(computeBonus.getStationId());//????????????
                txnBonus.setPost_id(computeBonus.getPosId());
                txnBonus.setStation_name(stationShortName);//????????????
                txnBonus.setTxn_amt(totalPrice);//????????????<-??????????????????????????????
                txnBonus.setChannel(channel);//??????
                txnBonus.setTxn_ssn_ora(computeBonus.getReqSerialNo());//????????????
                txnBonus.setTxn_date_ora(businessDate);//????????????
                txnBonus.setPay_typeSet(payTypeSet);//????????????
                txnBonus.setTxn_type("s");//????????????
                txnBonus.setMachine_id(null);//????????????
                if(SysConstant.GOODS_TYPE_NOT_OIL.equals(goodsType)){ //?????????
                    txnBonus.setGoods_ids(SysConstant.NOT_OIL_FLAG);//?????????
                    txnBonus.setGoods_name("?????????");//????????????
                    txnBonus.setGoods_mid_type(SysConstant.NOT_OIL_FLAG);//????????????
                }else{ //??????
                    txnBonus.setGoods_ids(goodsId);//????????????
                    txnBonus.setGoods_name(goodsName);//????????????
                    txnBonus.setGoods_mid_type(middleType);//????????????
                }
                txnBonus.setGoods_lit_type(litType);//????????????
                txnBonus.setGoods_num(goodsNumber);//	??????????????????
                txnBonus.setGoods_count(goodsNumber);//	??????????????????
                txnBonus.setGoods_unit_price(unitPrice);//	????????????
                txnBonus.setGoods_act_price(totalPrice);//????????????????????????
                txnBonus.setCust_level(custInf.getCustLevel());//????????????
                double levelCoefficientValue = 1d; //????????????
                Double levelValueDict = levelCoefficient.get(custInf.getCustLevel());
                if(levelValueDict != null){
                    levelCoefficientValue = levelValueDict;
                }
                txnBonus.setCust_level_coefficient(levelCoefficientValue);//????????????
                txnBonus.setMarketregion(region);
                txnBonus.setField5(computeBonus.getPosId());
                txnBonus.setField1(String.valueOf(custInf.getIsRealCust()));//????????????

                if(isWhite){
                    txnBonus.setAcct_id(unionPayCardNo);
                    txnBonus.setField3("1");// ???????????????????????????
                }

                if(StrUtil.isNotBlank(unionPayCardNo) && CollUtil.contains(diyCadBinValue, StrUtil.sub(unionPayCardNo, cardBinStart, cardBinEnd))){
                    txnBonus.setField4("1");//?????????????????????bin?????????????????????bin
                }

                txnBonus.setField2(label);//????????????
            }

            if(StrUtil.isNotBlank(birthday)){
                txnBonus.setBrithday(birthday);
            }
            txnBonus.convertData();//????????????

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
     *<p><strong>Description:</strong> ????????????????????????  </p>
     * @return
     * @author ZYK
     * @update ??????: 2015-5-6
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
     *<p><strong>Description:</strong>  ???????????????????????? </p>
     * @param txnBonus
     * @param v_result
     * @param ar
     * @throws IOException
     * @author <a href="mailto: zhan_yaokang@huateng.com">zhanyaokang</a>
     * @throws SQLException
     * @update ??????: 2012-9-25
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
     *<p><strong>Description:</strong> ??????????????????  </p>
     * @param txnBonus
     * @param result
     * @param ar
     * @throws IOException
     * @author <a href="mailto: zhan_yaokang@huateng.com">zhanyaokang</a>
     * @throws SQLException
     * @update ??????: 2012-9-25
     */
    private List<BonusAccItf>  printResult(List<BonusAccItf> bonusAccItfList, TxnBonus txnBonus,TxnBonusResult result, PackageResult.ActivityResult ar) {
        //int bonusPoint = result.getRoundBonusPoint();//????????????
        BigDecimal bonusPoint = result.getRoundBonusPoint();//????????????
        if (result.isExec && result.isEffect && bonusPoint.compareTo(BigDecimal.ZERO) > 0) {
            if (result.isExec && result.isEffect) {
                String activeId = ar.name != null ? ar.name.substring(2) : "";
                String ruleId = result.ruleName != null ? result.ruleName.substring(2) : "";

                BonusAccItf bonusAccItf = new BonusAccItf();
                bonusAccItf.setActivityId(activeId);//????????????
                bonusAccItf.setRuleId(ruleId);//????????????
                bonusAccItf.setGoodsId(txnBonus.getGoods_ids());//??????id
                bonusAccItf.setGoodsName(txnBonus.getGoods_name());//????????????
                bonusAccItf.setTxnBonus(bonusPoint);//????????????
                //bonusAccItf.setTxnBonus(NumberUtil.round(bonusPoint,2, RoundingMode.HALF_DOWN));//???????????? ??????????????????
                bonusAccItf.setValidDate(result.validDate);//???????????????
                bonusAccItf.setBpPlanType(result.bpPlanType);//????????????
                bonusAccItf.setTxnAmtOra(NumberUtil.round(txnBonus.getTxn_amt(),4, RoundingMode.HALF_DOWN));// txn_amt_ora ???????????????

                bonusAccItfList.add(bonusAccItf);
            }
        }
        return bonusAccItfList;
    }

    /**
     *  ????????????
     * @param bonusEnter
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public AddPointsResult bonusPlanEnter(BonusEnter bonusEnter) throws Exception{
        TblCustInf custInf = bonusEnter.getCustInf();//????????????
        String custId = custInf.getCustId();
        String lockName = SysConstant.LOCK_MEMBER_POINTS + custId;
        RLock rLock = this.redissonClient.getLock(lockName);
        BigDecimal totalTxnPoints = BigDecimal.ZERO;//???????????????

        /**
         * ???????????????????????????
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

            /**????????????????????????**/
            OrderInfo orderInfo = new OrderInfo();
            orderInfo.setOrderType(bonusEnter.getOrder().getOrderType());
            orderInfo.setChannel(bonusEnter.getOrder().getChannel());
            orderInfo.setReqSerialNo(bonusEnter.getOrder().getReqSerialNo());
            ServiceOrder serviceOrder = this.memberPointsRelateService.getOrderInfo(orderInfo);
            //?????????????????????????????????????????????
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
                log.info("?????????????????????????????????????????????{}",serviceOrder.getReqSerialNo());
                return result;
            }

            AddPointsResult addPointsResult = new AddPointsResult();//????????????
            ServiceOrder order = bonusEnter.getOrder();//????????????
            List<BonusAccItf> bonus = bonusEnter.getBonus();//??????????????????
            List<Goods> orderGoods = bonusEnter.getGoods();//??????????????????
            String txnDate = DateUtil.date2String(order.getChannelDate(), DateUtil.DATE_FORMAT_COMPACT);//????????????
            String txnTime = DateUtil.date2String(order.getChannelDate(), DateUtil.DATE_FORMAT_TIME);//????????????
            String expireDate = DateUtil.date2String(order.getHostDate(), DateUtil.DATE_YEAR) + SysConstant.YEAR_FLAG;//????????????????????????
            BigDecimal expireBonus = null;//?????????????????????

            if(CollUtil.isNotEmpty(bonus)){
                totalTxnPoints = bonus.stream().map(BonusAccItf::getTxnBonus).reduce(BigDecimal.ZERO,BigDecimal::add);
            }

            this.validateService.validateForObject(this.apolloControl.getLimitDay(),SysConstant.E02000012);//?????????????????????

           //???????????????
            String custType = custInf.getCustType();//????????????E?????????????????????
            if(totalTxnPoints.compareTo(BigDecimal.ZERO) > 0 && this.apolloControl.getLimitDay().equals(1) && !SysConstant.CUST_TYPE_ENTERPRISE.equals(custType)){
                BigDecimal limitDayCount = this.apolloControl.getLimitDayCount();
                String hostDate = DateUtil.getCurrentDateyyyyMMdd();
                ServiceCustLimit serviceCustLimit = new ServiceCustLimit();
                serviceCustLimit.setLimitType(SysConstant.CUSTOMER_LIMIT_TYPE_DAY);//????????????
                serviceCustLimit.setCustId(custId);//????????????
                ResInfo resInfo = this.queryLimitRemote.queryLimit(serviceCustLimit);//???????????????
                ServiceCustLimit limit = JacksonUtil.toObject(getResJson(resInfo),ServiceCustLimit.class);
                if(limit == null){
                    ServiceCustLimit saveLimit = new ServiceCustLimit();
                    saveLimit.setLimitType(serviceCustLimit.getLimitType());//????????????
                    saveLimit.setCustId(serviceCustLimit.getCustId());//????????????
                    saveLimit.setLimitKey(hostDate);
                    saveLimit.setLimitValue(BigDecimal.ONE);
                    serviceCustLimitMapper.insert(saveLimit);
                }else{
                    if(hostDate.equals(limit.getLimitKey())){
                        BigDecimal limitNewValue = limit.getLimitValue().add(BigDecimal.ONE);
                        if(limitNewValue.compareTo(limitDayCount) > 0){
                            throw new BusinessException(getErrorInfo(SysConstant.E02000013),new Object[]{custId, limitDayCount}); //?????????????????????????????????
                        }
                        limit.setLimitValue(limitNewValue);
                        serviceCustLimitMapper.updateByPrimaryKeySelective(limit);
                    }else{
                        //??????????????????1
                        limit.setLimitKey(hostDate);
                        limit.setLimitValue(BigDecimal.ONE);
                        serviceCustLimitMapper.updateByPrimaryKeySelective(limit);
                    }

                }
                serviceCustLimit = null;
                resInfo = null;
                limit = null;
            }

            //??????????????????
            ResInfo planResInfo = queryMemberPointsRemote.queryBonusPlanByCustId(custId, SysConstant.BP_PLAN_TYPE_DEFAULT);
            TblBonusPlan bonusPlan = JacksonUtil.toObject(getResJson(planResInfo), TblBonusPlan.class);
            planResInfo = null;
            if(bonusPlan == null){
                log.info("??????[{}]???????????????????????????[{}]???TblBonusPlan?????????????????????",custId,SysConstant.DEFAULT_USAGE_KEY);
                bonusPlan = new TblBonusPlan();
                bonusPlan.setUsageKey(SysConstant.DEFAULT_USAGE_KEY);
                bonusPlan.setCustId(custId);
                bonusPlan.setLockStatus(SysConstant.BONUS_PLAN_LOCK_STATUS_NORMAL);//??????????????????
                bonusPlan.setBpPlanType(SysConstant.BP_PLAN_TYPE_DEFAULT);//????????????
                bonusPlan.setTotalBonus(BigDecimal.ZERO);//???????????????
                bonusPlan.setValidBonus(BigDecimal.ZERO);//????????????
                bonusPlan.setApplyBonus(BigDecimal.ZERO);//????????????
                bonusPlan.setExpireBonus(BigDecimal.ZERO);//????????????
            }

            //??????????????????
            Set<String> updateValidate = new HashSet<>();//????????????????????????
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

            //??????????????????
            Map<String, ServiceDict> dictBonusDelay = getServiceDict().get(SysConstant.DICT_KEY_1005000);
            ServiceDict bonusDelay = dictBonusDelay.get(SysConstant.DICT_BONUS_DELAY);
            BigDecimal bonusDelayValue = new BigDecimal(bonusDelay.getDictValue());
            if(totalTxnPoints.compareTo(bonusDelayValue) > 0){
                ResInfo resInfo = queryMemberPointsRemote.queryCustBonusDelay(custId);
                TblBonusDelay tblBonusDelay = JacksonUtil.toObject(getResJson(resInfo), TblBonusDelay.class);
                resInfo = null;
                if(tblBonusDelay != null){
                    if(SysConstant.AUTO_REISSUE_BONUS_DELAY.equals(tblBonusDelay.getStatus())){
                        log.info("????????????TblBonusDelay...");
                        ServiceOrder delayOrder = new ServiceOrder();
                        BigDecimal txnBonus = tblBonusDelay.getTxnBonus();//????????????
                        String validDate = tblBonusDelay.getValidDate();//???????????????
                        Short operate = SysConstant.BONUS_FLAG_D.equals(tblBonusDelay.getBonusCdFlag()) ? SysConstant.BONUS_OPERATE_INCREASE : SysConstant.BONUS_OPERATE_DECREASE;
                        String msg = SysConstant.BONUS_FLAG_D.equals(tblBonusDelay.getBonusCdFlag()) ? SysConstant.MEMBER_POINTS_INCREASE_TYPE : SysConstant.MEMBER_POINTS_DECREASE_TYPE;
                        //????????????
                        delayOrder.setValidBefore(bonusPlan.getValidBonus());
                        //??????????????????
                        TblBonusPlanDetail planDetail = this.getPlanDetail(custId, SysConstant.BP_PLAN_TYPE_DEFAULT, bonusPlanDetailMap, validDate);
                        this.operatePlanAndDetail(bonusPlan,planDetail,operate,txnBonus);
                        updateValidate.add(validDate);
                        //????????????
                        delayOrder.setValidAfter(bonusPlan.getValidBonus());

                        //????????????
                        String orderId = getSnowId();
                        delayOrder.setId(orderId);//????????????
                        delayOrder.setChannel(SysConstant.SYSTEM_CHANNEL);//?????????????????????
                        delayOrder.setPosId(SysConstant.SYSTEM_CHANNEL);//pos??????
                        delayOrder.setStationId(order.getStationId());//???????????????????????????
                        delayOrder.setReqSerialNo(tblBonusDelay.getId());//??????????????????
                        delayOrder.setChannelDate(order.getChannelDate());//????????????
                        delayOrder.setBusinessDate(order.getBusinessDate());//????????????
                        delayOrder.setHostDate(order.getHostDate());//????????????
                        delayOrder.setNumber(txnBonus);//???????????????
                        delayOrder.setReturnableNumber(txnBonus);//????????????
                        delayOrder.setStatus(SysConstant.ORDER_STATUS_SUCCESS);//????????????
                        delayOrder.setOrderType(SysConstant.ORDER_TYPE_REISSUE);//????????????
                        delayOrder.setOperate(operate);
                        delayOrder.setCustId(custId);//custId
                        delayOrder.setAcctId(order.getAcctId());
                        serviceOrderMapper.insert(delayOrder);

                        //?????????????????????????????????????????????
                        Map<String, ServiceDict> dictMap = getServiceDict().get(SysConstant.DICT_KEY_1004000);
                        ServiceDict serviceDictAdjust = dictMap.get(SysConstant.DICT_KEY_POINTS_ADJUST);
                        String goodsName = MessageFormat.format(serviceDictAdjust.getDictValue(),
                                new Object[]{
                                        tblBonusDelay.getAdjustDesc(),//??????
                                        msg,//??????
                                        tblBonusDelay.getTxnBonus()//???????????????
                                });
                        ServiceOrderDetail delayOrderDetail = new ServiceOrderDetail();
                        delayOrderDetail.setId(tblBonusDelay.getId());//??????????????????
                        delayOrderDetail.setOrderId(orderId);
                        delayOrderDetail.setGoodsId(String.valueOf(orderId));//?????????id????????????id
                        delayOrderDetail.setTotalPrice(txnBonus);
                        delayOrderDetail.setUnitPrice(txnBonus);
                        delayOrderDetail.setNumber(BigDecimal.ONE);
                        delayOrderDetail.setReturnableNumber(BigDecimal.ONE);
                        delayOrderDetail.setGoodsName(goodsName);
                        delayOrderDetail.setGoodsType(SysConstant.GOODS_TYPE_REISSUE);
                        this.serviceOrderDetailMapper.insert(delayOrderDetail);

                        //??????????????????
                        ServiceBonusDetail newServiceBonusDetail = new ServiceBonusDetail();
                        newServiceBonusDetail.setOrderId(orderId);
                        newServiceBonusDetail.setOrderSerial(0);
                        newServiceBonusDetail.setOperate(operate);
                        newServiceBonusDetail.setValidDate(validDate);
                        newServiceBonusDetail.setNumber(txnBonus);
                        newServiceBonusDetail.setReturnableNumber(txnBonus);
                        this.serviceBonusDetailMapper.insert(newServiceBonusDetail);

                        //????????????????????????
                        tblBonusDelay.setModifyDate(DateUtil.date2String(order.getBusinessDate(),DateUtil.DATE_FORMAT_COMPACT));
                        tblBonusDelay.setModifyTime(txnTime);
                        tblBonusDelay.setBonusSsn(order.getId()+"");
                        tblBonusDelay.setStatus("1");
                        tblBonusDelayMapper.updateStatus(tblBonusDelay);

                        //??????????????????
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

            //???????????????????????????
            ResInfo orderDetailResInfo = this.queryMemberPointsRemote.queryNewOrderDetailByOrderId(order.getId());
            List<ServiceOrderDetail> orderDetailList = JacksonUtil.toObject(getResJson(orderDetailResInfo), new TypeReference<List<ServiceOrderDetail>>(){});
            if(orderDetailList == null || orderDetailList.isEmpty()){
                //??????????????????
                ServiceOrderDetail serviceOrderDetail;
//              TblFuelConsumption fuelConsumption;
                for (Goods goods : orderGoods) {
                    serviceOrderDetail = new ServiceOrderDetail();
                    serviceOrderDetail.setId(goods.getId());//?????????id
                    serviceOrderDetail.setOrderId(order.getId());//??????id
                    serviceOrderDetail.setGoodsId(goods.getGoodsId());//??????id
                    serviceOrderDetail.setTotalPrice(goods.getTotalPrice());//????????????
                    serviceOrderDetail.setUnitPrice(goods.getUnitPrice());//????????????
                    serviceOrderDetail.setMiddleType(String.valueOf(goods.getMiddleType()));//????????????
                    serviceOrderDetail.setLitType(String.valueOf(goods.getLitType()));//????????????
                    serviceOrderDetail.setNumber(goods.getNumber());//????????????
                    serviceOrderDetail.setReturnableNumber(goods.getNumber());//????????????
                    serviceOrderDetail.setGoodsName(goods.getGoodsName());//????????????
                    serviceOrderDetail.setGoodsType(goods.getGoodsType());//????????????
                    serviceOrderDetail.setDiscountType(goods.getDiscountType());//????????????
                    this.serviceOrderDetailMapper.insert(serviceOrderDetail);

                  //?????????????????? ??????????????? ????????????????????????20L
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

            //????????????????????????
            ServiceBonusDetail newServiceBonusDetail;
            int serialIndex = 0;
            if(bonus != null) {
                for (BonusAccItf bonusAccItf : bonus) {
                    //??????????????????
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

            //????????????????????????
//            if(bonus != null){
//                BigDecimal newTotalTxnPoints = BigDecimal.ZERO;
//                Map<String,BonusAccItf> optMap = new HashMap<String,BonusAccItf>();
//                List<BonusAccItf> newBonusAccItfList = new ArrayList<BonusAccItf>();
//                //????????????????????????
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
//                //????????????
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

            //????????????
            order.setValidBefore(bonusPlan.getValidBonus());//?????????
            if(bonus != null) {
                for (BonusAccItf bonusAccItf : bonus) {
                    String validDate = bonusAccItf.getValidDate();//?????????
                    BigDecimal txnBonus = bonusAccItf.getTxnBonus();//??????
                    TblBonusPlanDetail planDetail = this.getPlanDetail(custId, SysConstant.BP_PLAN_TYPE_DEFAULT, bonusPlanDetailMap, validDate);
                    this.operatePlanAndDetail(bonusPlan, planDetail, SysConstant.BONUS_OPERATE_INCREASE, txnBonus);
                    updateValidate.add(validDate);
                }
            }
            order.setValidAfter(bonusPlan.getValidBonus());//?????????

            //????????????
            order.setNumber(totalTxnPoints);
            order.setReturnableNumber(totalTxnPoints);
            order.setStatus(SysConstant.ORDER_STATUS_SUCCESS);
            this.serviceOrderMapper.updateByPrimaryKeySelective(order);

            //??????????????????????????????
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

            //???????????????????????????
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

            //???tbl_bonus_detail_account??? ??????????????????
            this.toRmsBonusData(bonusEnter);

            //??????????????????
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


            //?????????????????????
            TblBonusPlanDetail planDetail = bonusPlanDetailMap.get(expireDate);
            if(planDetail != null){
                expireBonus = NumberUtil.null2Zero(planDetail.getValidBonus());
            }

            addPointsResult.setCustId(custId);//??????id
            addPointsResult.setTaxName(custInf.getCustInvoice());//????????????
            addPointsResult.setIsAcceptEInvoice(custInf.getIsAcceptEinvoice());//?????????????????????????????????
            addPointsResult.setValidBonus(bonusPlan.getValidBonus());//?????????????????????
            addPointsResult.setTxnBonus(totalTxnPoints);//????????????
            addPointsResult.setWillExpireDate(expireDate);//????????????????????????
            addPointsResult.setWillExpireBonus(NumberUtil.null2Zero(expireBonus));//?????????????????????
            return addPointsResult;
        }catch (Exception e){
            throw e;
        }finally {
            rLock.unlock();
            orderLock.unlock();
        }
    }

    /**
     * ???tbl_bonus_detail_account???
     * @param bonusEnter
     */
    private void toRmsBonusData(BonusEnter bonusEnter) throws Exception{
        List<TblBonusDetailAccount> dataList = new ArrayList<>();
        TblCustInf custInf = bonusEnter.getCustInf();
        ServiceOrder order = bonusEnter.getOrder();
        List<BonusAccItf> bonusResultList = bonusEnter.getBonus();//??????????????????
        if(CollUtil.isNotEmpty(bonusResultList)){
            Map<Integer, ServiceNotProduceMidtype> notProduceMidtypeMap = this.getNotProduceMidtypeMap();
            List<Goods> goods = this.analysisNoPointsList(bonusEnter.getGoods(),notProduceMidtypeMap);
            Map<Short, List<Goods>> singleMap = goods.stream().collect(Collectors.groupingBy(Goods::getGoodsType)); //???????????????????????????
            for (BonusAccItf bonusAccItf : bonusResultList) {
                if(SysConstant.NOT_OIL_FLAG.equals(bonusAccItf.getGoodsId())){
                    /** ??????????????? **/
                    List<Goods> notOilGoodsList = singleMap.get(SysConstant.GOODS_TYPE_NOT_OIL);
                    if(CollUtil.isNotEmpty(notOilGoodsList)){
                        BigDecimal surplusBonus = BigDecimal.ZERO;//???????????? ????????????????????????
                        BigDecimal surplusPrice = BigDecimal.ZERO;//?????????????????? ????????????????????????
                        BigDecimal txnBonus = bonusAccItf.getTxnBonus();//????????????????????????????????????
                        BigDecimal txnPrice = bonusAccItf.getTxnBonus().multiply(SysConstant.BONUS_COST);//??????????????????????????????????????????
                        BigDecimal txnAmtOra = bonusAccItf.getTxnAmtOra();//???????????????
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
                            tbda.setBonusType("0"); //????????????????????????2
                            tbda.setRuleId(bonusAccItf.getRuleId());
                            tbda.setGoodsType("0");
                            //tbda.setRuleStationId();//??????????????????

                            Double ratio = notOilGoods.getTotalPrice().doubleValue() / txnAmtOra.doubleValue();
                            BigDecimal userBonus;//???????????????????????????
                            BigDecimal userPrice;//??????????????????????????????
                            if(i == (notOilGoodsList.size() -1)){
                                userBonus = txnBonus.subtract(surplusBonus);
                                userPrice = txnPrice.subtract(surplusPrice);
                                tbda.setTxnBonus(userBonus);//???????????????
                                tbda.setTxnBonusPrice(userPrice);//????????????
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
                    /** ???????????? **/
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
                                //tbda.setRuleStationId();//??????????????????
                                tbda.setTxnBonus(bonusAccItf.getTxnBonus());//???????????????
                                tbda.setTxnBonusPrice(bonusAccItf.getTxnBonus().multiply(SysConstant.BONUS_COST));//????????????

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


    //???????????????????????????????????????map???
    private TblBonusPlanDetail getPlanDetail(String custId,String bpPlanType,Map<String,TblBonusPlanDetail> bonusPlanDetailMap,String validDate){
        TblBonusPlanDetail operatePlanDetail = bonusPlanDetailMap.get(validDate);
        if(operatePlanDetail == null){
            log.info("??????[{}]???????????????????????????[{}]????????????[{}]???tblBonusPlanDetail?????????????????????",custId,bpPlanType,validDate);
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

            bonusPlanDetailMap.put(validDate,operatePlanDetail);//??????
        }
        return operatePlanDetail;
    }
    //??????????????????
    private void operatePlanAndDetail(TblBonusPlan bonusPlan,TblBonusPlanDetail bonusPlanDetail,Short operate,BigDecimal txnBonus){
        if (SysConstant.BONUS_OPERATE_INCREASE.equals(operate)){
            bonusPlanDetail.setTotalBonus(bonusPlanDetail.getTotalBonus().add(txnBonus));//????????????
            bonusPlanDetail.setValidBonus(bonusPlanDetail.getValidBonus().add(txnBonus));//??????????????????
            bonusPlan.setTotalBonus(bonusPlan.getTotalBonus().add(txnBonus));//????????????
            bonusPlan.setValidBonus(bonusPlan.getValidBonus().add(txnBonus));//??????????????????
        }else{
            bonusPlanDetail.setApplyBonus(bonusPlanDetail.getApplyBonus().add(txnBonus));//??????????????????
            bonusPlanDetail.setValidBonus(bonusPlanDetail.getValidBonus().subtract(txnBonus));//????????????
            bonusPlan.setApplyBonus(bonusPlan.getApplyBonus().add(txnBonus));//??????????????????
            bonusPlan.setValidBonus(bonusPlan.getValidBonus().subtract(txnBonus));//????????????
        }
    }

    /**
     * ????????????-????????????
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
                throw new BusinessException(getErrorInfo(SysConstant.SYS_LOCK_ERROR));//???????????????
            }

            /**???????????????**/
            ResInfo orderResInfo = this.queryMemberPointsRemote.queryOrderById(autoBonusReversal.getTargetOrderId());
            ServiceOrder order = JacksonUtil.toObject(getResJson(orderResInfo), ServiceOrder.class);
            orderResInfo = null;

            /**?????????????????????**/
            ResInfo orderDetailResInfo = this.queryMemberPointsRemote.queryNewOrderDetailByOrderId(order.getId());
            List<ServiceOrderDetail> orderDetailList = JacksonUtil.toObject(getResJson(orderDetailResInfo), new TypeReference<List<ServiceOrderDetail>>(){});
            orderDetailResInfo = null;

            /**?????????????????????**/
            ResInfo bonusDetailResInfo = this.queryMemberPointsRemote.queryNewBonusDetailByOrderId(order.getId());
            List<ServiceBonusDetail> bonusDetailList = JacksonUtil.toObject(getResJson(bonusDetailResInfo), new TypeReference<List<ServiceBonusDetail>>(){});
            bonusDetailResInfo = null;

            String orderId = autoBonusReversal.getOrderId();//????????????id
            String hostDate = DateUtil.date2String(autoBonusReversal.getHostDateTime(), DateUtil.DATE_FORMAT_COMPACT);
            String hostTime = DateUtil.date2String(autoBonusReversal.getHostDateTime(), DateUtil.DATE_FORMAT_TIME);
            String yearFlag = DateUtil.getCurrentDateyyyy() + SysConstant.YEAR_FLAG;

            /**?????????**/
            if(order.getReturnableNumber().compareTo(BigDecimal.ZERO) == 0){
                serviceOrder = new ServiceOrder();
                serviceOrder.setId(orderId);
                serviceOrder.setStatus(SysConstant.ORDER_STATUS_SUCCESS);
                this.serviceOrderMapper.updateByPrimaryKeySelective(serviceOrder);
                serviceOrder = null;
                return;
            }

            //??????????????????
            ResInfo planResInfo = this.queryMemberPointsRemote.queryBonusPlanByCustId(autoBonusReversal.getCustId(),SysConstant.BP_PLAN_TYPE_DEFAULT);
            TblBonusPlan bonusPlan = JacksonUtil.toObject(getResJson(planResInfo), TblBonusPlan.class);
            planResInfo = null;
            BigDecimal validBefore = bonusPlan.getValidBonus();//???????????????

            /*if(order.getOrderType().equals(SysConstant.ORDER_TYPE_PRODUCE)){//????????????????????????????????????????????????
                if (bonusPlan.getValidBonus().compareTo(autoBonusReversal.getReverseNumber()) < 0) {
                    throw new BusinessException(getErrorInfo(SysConstant.E02000004));//??????????????????
                }
            }*/


            /**??????????????????**/
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


            //???????????????????????????
            List<ServiceBonusDetail> returnList = this.bonusAutoCeiling(bonusDetailList);
            //?????????????????????????????????0
            this.serviceBonusDetailMapper.updateReturnableNumberToZeroByOrderId(order.getId());

            //??????????????????????????????
            BigDecimal extendValidateNumber = BigDecimal.ZERO;//??????????????????????????????
            int serial = 0;
            /**???????????????????????????**/
            for (ServiceBonusDetail bonusDetail : returnList) {
                String validDate = bonusDetail.getValidDate();//???????????????
                BigDecimal bonusDetailNumber = bonusDetail.getReturnableNumber();//??????????????????
                if(bonusDetailNumber.compareTo(BigDecimal.ZERO) == 0){ //?????????????????????0????????????
                    continue;
                }

                TblBonusPlanDetail bonusPlanDetail = bonusPlanDetailMap.get(validDate);//???????????????????????????????????????????????????
                boolean isOverdue = DateUtil.isBefore4LocalDate(validDate, yearFlag, DateUtil.DATE_FORMAT_COMPACT);//????????????????????????
                if(isOverdue){
                    bonusPlanDetail.setExpiredStatus(SysConstant.BONUS_PLAN_DETAIL_EXPIRED);//????????????????????????
                    bonusPlanDetail.setTotalBonus(bonusPlanDetail.getTotalBonus().subtract(bonusDetailNumber));
                    bonusPlanDetail.setApplyBonus(bonusPlanDetail.getApplyBonus().subtract(bonusDetailNumber));
                    extendValidateNumber = bonusDetailNumber;
                }else{
                    bonusPlanDetail.setApplyBonus(bonusPlanDetail.getApplyBonus().subtract(bonusDetailNumber));
                    bonusPlanDetail.setValidBonus(bonusPlanDetail.getValidBonus().add(bonusDetailNumber));
                }

                bonusPlan.setApplyBonus(bonusPlan.getApplyBonus().subtract(bonusDetailNumber));
                bonusPlan.setValidBonus(bonusPlan.getValidBonus().add(bonusDetailNumber));

                //???????????????????????????
                bonusPlanDetail.setModifyDate(hostDate);
                bonusPlanDetail.setModifyTime(hostTime);
                this.tblBonusPlanDetailMapper.updatePlanDetailBonus(bonusPlanDetail);

                if(!isOverdue){
                    //?????????????????????????????????
                    ServiceBonusDetail serviceBonusDetail = new ServiceBonusDetail();
                    serviceBonusDetail.setOrderId(orderId);//???????????????id
                    serviceBonusDetail.setOrderSerial(serial);
                    serviceBonusDetail.setOperate(SysConstant.BONUS_OPERATE_INCREASE);
                    serviceBonusDetail.setValidDate(validDate);
                    serviceBonusDetail.setNumber(bonusDetailNumber);
                    this.serviceBonusDetailMapper.insert(serviceBonusDetail);
                }

                serial++;
            }

            //????????????
            this.autoExtension(extendValidateNumber,bonusPlanDetailMap,yearFlag,hostDate,hostTime,autoBonusReversal.getCustId(),autoBonusReversal.getOrderId(),serial);

            //????????????????????????
            bonusPlan.setModifyDate(hostDate);
            bonusPlan.setModifyTime(hostTime);
            this.tblBonusPlanMapper.updatePlanBonus(bonusPlan);

            //??????????????????????????????????????????????????????????????????
            for (ServiceOrderDetail orderDetail : orderDetailList) {
                BigDecimal returnableNumber = orderDetail.getReturnableNumber();
                if(returnableNumber.compareTo(BigDecimal.ZERO) == 0){//???????????????0????????????
                    continue;
                }

                ServiceOrderDetail saveOrderDetail = new ServiceOrderDetail();
                saveOrderDetail.setId(orderDetail.getId());//?????????id
                saveOrderDetail.setOrderId(orderId);//????????????id
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
                this.serviceOrderDetailMapper.updateReturnableNumber(orderDetail);//??????????????????????????????

            }
            //???????????????????????????
            serviceOrder = new ServiceOrder();
            serviceOrder.setId(autoBonusReversal.getTargetOrderId());
            serviceOrder.setStatus(SysConstant.ORDER_STATUS_REVERSAL_FULL);//????????????????????????
            serviceOrder.setReturnableNumber(BigDecimal.ZERO);
            this.serviceOrderMapper.updateByPrimaryKeySelective(serviceOrder);


            //???????????????????????????
            serviceOrder = new ServiceOrder();
            serviceOrder.setValidBefore(validBefore);//??????????????????
            serviceOrder.setValidAfter(bonusPlan.getValidBonus());//??????????????????
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
     * ???????????????????????????
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
     * ????????????-???????????????
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
                throw new BusinessException(getErrorInfo(SysConstant.SYS_LOCK_ERROR));//???????????????
            }

            ResInfo orderResInfo = this.queryMemberPointsRemote.queryTblOrderByOrderId(autoTblBonusReversal.getTargetOrderId());
            TblOrder order = JacksonUtil.toObject(getResJson(orderResInfo),TblOrder.class);//????????????
            orderResInfo = null;

            ResInfo txnDetailResInfo = this.queryMemberPointsRemote.queryTxnDetailByAcqSsn(order.getAcqSsn());
            TblTxnDetail txnDetail = JacksonUtil.toObject(getResJson(txnDetailResInfo),TblTxnDetail.class);//??????????????????
            txnDetailResInfo = null;

            ResInfo tblOrderDetailResInfo = this.queryMemberPointsRemote.queryOrderDetailByOrderId(order.getOrderId());
            List<TblOrderDetail> tblOrderDetailList = JacksonUtil.toObject(getResJson(tblOrderDetailResInfo),new TypeReference<List<TblOrderDetail>>(){});//????????????
            tblOrderDetailResInfo = null;

            ResInfo tblBonusDetailResInfo = this.queryMemberPointsRemote.queryBonusDetailByBonusSsn(order.getBonusSsn());
            List<TblBonusDetail> tblBonusDetailList = JacksonUtil.toObject(getResJson(tblBonusDetailResInfo),new TypeReference<List<TblBonusDetail>>(){});//??????????????????
            tblBonusDetailResInfo = null;

            String orderId = autoTblBonusReversal.getOrderId();//????????????id
            String custId = autoTblBonusReversal.getCustId();//??????id
            String hostDate = DateUtil.date2String(autoTblBonusReversal.getHostDateTime(), DateUtil.DATE_FORMAT_COMPACT);//????????????
            String hostTime = DateUtil.date2String(autoTblBonusReversal.getHostDateTime(), DateUtil.DATE_FORMAT_TIME);//????????????
            String yearFlag = DateUtil.getCurrentDateyyyy() + SysConstant.YEAR_FLAG;

            /**?????????**/
            if(order.getOrderBonus().compareTo(BigDecimal.ZERO) == 0){
                serviceOrder = new ServiceOrder();
                serviceOrder.setId(autoTblBonusReversal.getOrderId());
                serviceOrder.setStatus(SysConstant.ORDER_STATUS_SUCCESS);
                this.serviceOrderMapper.updateByPrimaryKeySelective(serviceOrder);
                serviceOrder = null;
                return;
            }

            //??????????????????
            ResInfo planResInfo = queryMemberPointsRemote.queryBonusPlanByCustId(custId, SysConstant.BP_PLAN_TYPE_DEFAULT);
            TblBonusPlan bonusPlan = JacksonUtil.toObject(getResJson(planResInfo), TblBonusPlan.class);
            planResInfo = null;
            serviceOrder = new ServiceOrder();
            serviceOrder.setValidBefore(bonusPlan.getValidBonus());//???????????????

            //??????????????????
            BonusPlanDetailParams bonusPlanDetailParams = new BonusPlanDetailParams();
            bonusPlanDetailParams.setCustId(custId);
            bonusPlanDetailParams.setBpPlanType(SysConstant.BP_PLAN_TYPE_DEFAULT);
            ResInfo planDetailsResInfo = this.queryMemberPointsRemote.queryBonusPlanDetail(bonusPlanDetailParams);
            List<TblBonusPlanDetail> bonusPlanDetailList = JacksonUtil.toObject(getResJson(planDetailsResInfo),new TypeReference<List<TblBonusPlanDetail>>(){});
            Map<String, TblBonusPlanDetail> bonusPlanDetailMap = bonusPlanDetailList.stream().collect(Collectors.toMap(TblBonusPlanDetail::getValidDate, Function.identity()));
            bonusPlanDetailParams = null;
            planDetailsResInfo = null;

            //??????????????????
            int serialIndex = 0;
            TblBonusDetail tblBonusDetailParam;
            BigDecimal extendValidateNumber = BigDecimal.ZERO;
            Short operate = null;
            for (TblBonusDetail tblBonusDetail : tblBonusDetailList) {
                String validDate = tblBonusDetail.getValidDate();//??????????????????
                String bonusCdFlag = tblBonusDetail.getBonusCdFlag();//???????????????c???????????????d????????????
                BigDecimal txnBonus = tblBonusDetail.getTxnBonus();//????????????
                if(txnBonus.compareTo(BigDecimal.ZERO) == 0){ //?????????????????????0????????????
                    continue;
                }

                TblBonusPlanDetail planDetail = bonusPlanDetailMap.get(validDate);
                operate = "c".equals(bonusCdFlag) ? SysConstant.BONUS_OPERATE_INCREASE : SysConstant.BONUS_OPERATE_DECREASE; //????????????
                boolean isOverdue = DateUtil.isBefore4LocalDate(validDate, yearFlag, DateUtil.DATE_FORMAT_COMPACT);//???????????????????????????????????????

                boolean isExtend = false;//????????????????????????????????????
                if(SysConstant.BONUS_OPERATE_INCREASE.equals(operate)){ //????????????????????????
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
                }else{//????????????????????????
                    planDetail.setTotalBonus(planDetail.getTotalBonus().subtract(txnBonus));
                    planDetail.setValidBonus(planDetail.getValidBonus().subtract(txnBonus));

                    bonusPlan.setTotalBonus(bonusPlan.getTotalBonus().subtract(txnBonus));
                    bonusPlan.setValidBonus(bonusPlan.getValidBonus().subtract(txnBonus));
                }

                //?????????????????????
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

                //?????????????????????
                tblBonusDetailParam = new TblBonusDetail();
                tblBonusDetailParam.setPkBonusDetail(tblBonusDetail.getPkBonusDetail());
                tblBonusDetailParam.setReturnFlag("1");
                this.tblBonusDetailMapper.updateReturnFlagByPrimaryKey(tblBonusDetailParam);
                serialIndex++;
            }
            tblBonusDetailParam = null;

            //????????????
            this.autoExtension(extendValidateNumber,bonusPlanDetailMap,yearFlag,hostDate,hostTime,custId,orderId,serialIndex);

            //????????????????????????
            bonusPlan.setModifyDate(hostDate);
            bonusPlan.setModifyTime(hostTime);
            this.tblBonusPlanMapper.updatePlanBonus(bonusPlan);

            //????????????
            TblOrderDetail updateOrderDetailParam;
            for (TblOrderDetail tblOrderDetail : tblOrderDetailList) {
                BigDecimal orderNum = tblOrderDetail.getOrderNum();//??????????????????
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

            //????????????????????????
            txnDetail.setReturnFlag("1");
            this.tblTxnDetailMapper.updateReturnFlag(txnDetail);

            //????????????
            order.setOrderStatus("01");
            order.setReturnTime(DateUtil.date2String(autoTblBonusReversal.getHostDateTime(), DateUtil.DATE_FORMAT_COMPACT));
            this.tblOrderMapper.updateStatus(order);

            //??????????????????
            serviceOrder.setValidAfter(bonusPlan.getValidBonus());//??????????????????
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
     * ????????????-????????????
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
                throw new BusinessException(getErrorInfo(SysConstant.SYS_LOCK_ERROR));//???????????????
            }

            /**???????????????**/
            ResInfo orderResInfo = this.queryMemberPointsRemote.queryOrderById(autoBonusReversal.getTargetOrderId());
            ServiceOrder order = JacksonUtil.toObject(getResJson(orderResInfo), ServiceOrder.class);
            orderResInfo = null;

            /**?????????????????????**/
            ResInfo orderDetailResInfo = this.queryMemberPointsRemote.queryNewOrderDetailByOrderId(order.getId());
            List<ServiceOrderDetail> orderDetailList = JacksonUtil.toObject(getResJson(orderDetailResInfo), new TypeReference<List<ServiceOrderDetail>>(){});
            orderDetailResInfo = null;

            /**?????????????????????**/
            ResInfo bonusDetailResInfo = this.queryMemberPointsRemote.queryNewBonusDetailByOrderId(order.getId());
            List<ServiceBonusDetail> bonusDetailList = JacksonUtil.toObject(getResJson(bonusDetailResInfo), new TypeReference<List<ServiceBonusDetail>>(){});
            bonusDetailResInfo = null;

            String hostDate = DateUtil.date2String(autoBonusReversal.getHostDateTime(), DateUtil.DATE_FORMAT_COMPACT);//????????????
            String hostTime = DateUtil.date2String(autoBonusReversal.getHostDateTime(), DateUtil.DATE_FORMAT_TIME);//????????????
            String yearFlag = DateUtil.getCurrentDateyyyy()+SysConstant.YEAR_FLAG;//???????????????????????????????????????
            BigDecimal reverseNumber = autoBonusReversal.getReverseNumber();//???????????????
            String orderId = autoBonusReversal.getOrderId();//????????????id
            String custId = autoBonusReversal.getCustId();//??????id

            if(reverseNumber.compareTo(order.getReturnableNumber()) > 0){
                throw new BusinessException(getErrorInfo(SysConstant.E02000007));//???????????????????????????????????????????????????
            }

            //?????????????????????map<?????????id,??????>
            Map<String, ServiceOrderDetail> orderDetailMap = orderDetailList.stream().collect(Collectors.toMap(ServiceOrderDetail::getId, Function.identity()));
            boolean flag = true;//?????????????????????
            BigDecimal returnNumber = BigDecimal.ZERO;//?????????????????????????????????????????????
            for (ReversalDetail detail : autoBonusReversal.getDetails()) {
                String id = detail.getId();//???????????????id
                BigDecimal goodsNumber = detail.getNumber();//??????????????????
                ServiceOrderDetail orderDetail = orderDetailMap.get(id);//???????????????id??????????????????

                if(orderDetail == null){
                    throw new BusinessException(getErrorInfo(SysConstant.E02000011),new Object[]{id});//???????????????{0}????????????
                }

                //??????????????????
                if(goodsNumber.compareTo(orderDetail.getReturnableNumber()) > 0){
                    throw new BusinessException(getErrorInfo(SysConstant.E02000010),new Object[]{id});//????????????[{0}]?????????????????????????????????????????????
                }

                returnNumber = returnNumber.add(orderDetail.getUnitPrice().multiply(goodsNumber));
                if(StrUtil.isNotBlank(orderDetail.getDiscountType())){
                    flag = false;//???????????????
                }

                //????????????????????????
                ServiceOrderDetail saveOrderDetail = new ServiceOrderDetail();
                saveOrderDetail.setId(id);//?????????id
                saveOrderDetail.setOrderId(orderId);//????????????id
                saveOrderDetail.setGoodsId(orderDetail.getGoodsId());
                saveOrderDetail.setTotalPrice(detail.getTotalPrice());
                saveOrderDetail.setUnitPrice(orderDetail.getUnitPrice());
                saveOrderDetail.setMiddleType(orderDetail.getMiddleType());
                saveOrderDetail.setLitType(orderDetail.getLitType());
                saveOrderDetail.setNumber(goodsNumber);//????????????????????????
                saveOrderDetail.setGoodsName(orderDetail.getGoodsName());
                saveOrderDetail.setGoodsType(orderDetail.getGoodsType());
                saveOrderDetail.setDiscountType(orderDetail.getDiscountType());
                this.serviceOrderDetailMapper.insert(saveOrderDetail);

                //??????????????????????????????
                orderDetail.setReturnableNumber(orderDetail.getReturnableNumber().subtract(goodsNumber));
                this.serviceOrderDetailMapper.updateReturnableNumber(orderDetail);
            }

            //?????????????????????????????????????????? ??? ??????????????????????????????
            if(flag){
                if(reverseNumber.compareTo(returnNumber) != 0){
                    throw new BusinessException(getErrorInfo(SysConstant.E02000008));//????????????????????????????????????
                }
            }

            //??????????????????
            ResInfo planResInfo = this.queryMemberPointsRemote.queryBonusPlanByCustId(autoBonusReversal.getCustId(),SysConstant.BP_PLAN_TYPE_DEFAULT);
            TblBonusPlan bonusPlan = JacksonUtil.toObject(getResJson(planResInfo), TblBonusPlan.class);
            planResInfo = null;
            BigDecimal validBefore = bonusPlan.getValidBonus();//???????????????????????????

            //????????????????????????????????????
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
            BigDecimal surplusBonus = reverseNumber;//??????????????????
            BigDecimal extendValidateNumber = BigDecimal.ZERO;//??????????????????????????????
            //????????????????????? ????????????????????????
            List<ServiceBonusDetail> returnList = bonusDetailList.stream().sorted(Comparator.comparing(ServiceBonusDetail::getValidDate).reversed()).collect(Collectors.toList());
            for (ServiceBonusDetail bonusDetail : returnList) {
                if(surplusBonus.compareTo(BigDecimal.ZERO) == 0){ //??????????????????
                    break;
                }
                if(bonusDetail.getReturnableNumber().compareTo(BigDecimal.ZERO) == 0){ //?????????????????????0??????
                    continue;
                }
                BigDecimal operateBonus;//??????????????????
                String validDate = bonusDetail.getValidDate();//???????????????
                BigDecimal bonusDetailNumber = bonusDetail.getReturnableNumber();//??????????????????
                if(surplusBonus.compareTo(bonusDetailNumber) > 0){ //??????????????????>????????????
                    operateBonus = bonusDetailNumber;
                }else{
                    operateBonus = surplusBonus;
                }

                Short saveOperate = SysConstant.BONUS_OPERATE_INCREASE;//????????????
                TblBonusPlanDetail bonusPlanDetail = bonusPlanDetailMap.get(validDate);//???????????????????????????????????????????????????
                boolean isOverdue = DateUtil.isBefore4LocalDate(validDate, yearFlag, DateUtil.DATE_FORMAT_COMPACT);//?????????????????????????????????

                if(isOverdue){//????????????????????????????????????,????????????????????????
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

                //???????????????????????????
                bonusPlanDetail.setModifyDate(hostDate);
                bonusPlanDetail.setModifyTime(hostTime);
                this.tblBonusPlanDetailMapper.updatePlanDetailBonus(bonusPlanDetail);

                //?????????????????????????????????
                if(!isOverdue){
                    ServiceBonusDetail serviceBonusDetail = new ServiceBonusDetail();
                    serviceBonusDetail.setOrderId(orderId);
                    serviceBonusDetail.setOrderSerial(serial);
                    serviceBonusDetail.setOperate(saveOperate);
                    serviceBonusDetail.setValidDate(validDate);
                    serviceBonusDetail.setNumber(operateBonus);
                    this.serviceBonusDetailMapper.insert(serviceBonusDetail);
                }

                //?????????????????????????????????
                bonusDetail.setReturnableNumber(bonusDetail.getReturnableNumber().subtract(operateBonus));
                this.serviceBonusDetailMapper.updateReturnableNumber(bonusDetail);

                surplusBonus = surplusBonus.subtract(operateBonus);
                serial++;
            }

            //????????????
            this.autoExtension(extendValidateNumber,bonusPlanDetailMap,yearFlag,hostDate,hostTime,custId,orderId,serial);


            //????????????????????????
            bonusPlan.setModifyDate(hostDate);
            bonusPlan.setModifyTime(hostTime);
            this.tblBonusPlanMapper.updatePlanBonus(bonusPlan);

            //???????????????????????????
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

            //???????????????????????????
            serviceOrder = new ServiceOrder();
            serviceOrder.setId(orderId);
            serviceOrder.setValidBefore(validBefore);
            serviceOrder.setValidAfter(bonusPlan.getValidBonus());//??????????????????
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
     * ????????????-???????????????
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
                throw new BusinessException(getErrorInfo(SysConstant.SYS_LOCK_ERROR));//???????????????
            }

            ResInfo orderResInfo = this.queryMemberPointsRemote.queryTblOrderByOrderId(autoTblBonusReversal.getTargetOrderId());
            TblOrder order = JacksonUtil.toObject(getResJson(orderResInfo),TblOrder.class);//????????????
            orderResInfo = null;

            ResInfo txnDetailResInfo = this.queryMemberPointsRemote.queryTxnDetailByAcqSsn(order.getAcqSsn());
            TblTxnDetail txnDetail = JacksonUtil.toObject(getResJson(txnDetailResInfo),TblTxnDetail.class);//??????????????????
            txnDetailResInfo = null;

            ResInfo tblOrderDetailResInfo = this.queryMemberPointsRemote.queryOrderDetailByOrderId(order.getOrderId());
            List<TblOrderDetail> tblOrderDetailList = JacksonUtil.toObject(getResJson(tblOrderDetailResInfo),new TypeReference<List<TblOrderDetail>>(){});//????????????
            tblOrderDetailResInfo = null;

            ResInfo tblBonusDetailResInfo = this.queryMemberPointsRemote.queryBonusDetailByBonusSsn(order.getBonusSsn());
            List<TblBonusDetail> tblBonusDetailList = JacksonUtil.toObject(getResJson(tblBonusDetailResInfo),new TypeReference<List<TblBonusDetail>>(){});//??????????????????
            tblBonusDetailResInfo = null;

            String hostDate = DateUtil.date2String(autoTblBonusReversal.getHostDateTime(), DateUtil.DATE_FORMAT_COMPACT);//????????????
            String hostTime = DateUtil.date2String(autoTblBonusReversal.getHostDateTime(), DateUtil.DATE_FORMAT_TIME);//????????????
            String yearFlag = DateUtil.getCurrentDateyyyy() + SysConstant.YEAR_FLAG;//?????????????????????????????????????????????
            BigDecimal reverseNumber = autoTblBonusReversal.getReverseNumber();//????????????
            String orderId = autoTblBonusReversal.getOrderId();//????????????id
            String custId = autoTblBonusReversal.getCustId();//??????id

            if(reverseNumber.compareTo(order.getOrderBonus()) > 0){
                throw new BusinessException(getErrorInfo(SysConstant.E02000007));//???????????????????????????????????????????????????
            }

            //??????????????????map<???????????????,??????>
            Map<String, TblOrderDetail> orderDetailMap = tblOrderDetailList.stream().collect(Collectors.toMap(TblOrderDetail::getOrderDetailId, Function.identity()));
            TblOrderDetail orderDetailParam;
            for (ReversalDetail detail : autoTblBonusReversal.getDetails()) {//??????????????????
                String id = detail.getId();//??????????????????
                BigDecimal goodsNumber = detail.getNumber();//??????????????????
                TblOrderDetail tblOrderDetail = orderDetailMap.get(id);

                if(tblOrderDetail == null){
                    continue;
                    //throw new BusinessException(getErrorInfo(SysConstant.E02000011),new Object[]{id});//???????????????{0}????????????
                }

                //??????????????????
                if(goodsNumber.compareTo(tblOrderDetail.getOrderNum()) > 0){
                    throw new BusinessException(getErrorInfo(SysConstant.E02000010),new Object[]{id});//????????????[{0}]?????????????????????????????????????????????
                }

                //????????????????????????
                ServiceOrderDetail saveOrderDetail = new ServiceOrderDetail();
                saveOrderDetail.setOrderId(orderId);//????????????id
                saveOrderDetail.setId(id);//???????????????id
                saveOrderDetail.setGoodsId(tblOrderDetail.getGoodsId());//????????????id
                saveOrderDetail.setTotalPrice(detail.getTotalPrice());
                saveOrderDetail.setUnitPrice(BigDecimal.ZERO);//???????????????????????????
                saveOrderDetail.setNumber(goodsNumber);//??????????????????
                saveOrderDetail.setReturnableNumber(goodsNumber);//??????????????????
                saveOrderDetail.setGoodsName(tblOrderDetail.getGoodsNm());//??????????????????
                saveOrderDetail.setGoodsType(SysConstant.GOODS_TYPE_NOT_OIL);//?????????
                this.serviceOrderDetailMapper.insert(saveOrderDetail);

                //????????????????????????????????????????????????????????????
                orderDetailParam = new TblOrderDetail();
                orderDetailParam.setPkOrderDtl(tblOrderDetail.getPkOrderDtl());
                orderDetailParam.setOrderCancel("0");
                orderDetailParam.setReturnTime(hostDate);
                this.tblOrderDetailMapper.updateByPrimaryKeySelective(orderDetailParam);
            }
            orderDetailParam = null;

            //??????????????????
            ResInfo planResInfo = this.queryMemberPointsRemote.queryBonusPlanByCustId(custId,SysConstant.BP_PLAN_TYPE_DEFAULT);
            TblBonusPlan bonusPlan = JacksonUtil.toObject(getResJson(planResInfo), TblBonusPlan.class);
            planResInfo = null;
            serviceOrder = new ServiceOrder();
            serviceOrder.setValidBefore(bonusPlan.getValidBonus());//???????????????????????????

            //????????????????????????????????????
            BonusPlanDetailParams bonusPlanDetailParams = new BonusPlanDetailParams();
            bonusPlanDetailParams.setCustId(custId);//??????id
            bonusPlanDetailParams.setBpPlanType(SysConstant.BP_PLAN_TYPE_DEFAULT);//????????????
            bonusPlanDetailParams.setIsOrderByDate(1);//??????????????????
            ResInfo planDetailsResInfo = this.queryMemberPointsRemote.queryBonusPlanDetail(bonusPlanDetailParams);
            List<TblBonusPlanDetail> bonusPlanDetailList = JacksonUtil.toObject(getResJson(planDetailsResInfo),new TypeReference<List<TblBonusPlanDetail>>(){});
            Map<String, TblBonusPlanDetail> bonusPlanDetailMap = bonusPlanDetailList.stream().collect(Collectors.toMap(TblBonusPlanDetail::getValidDate, Function.identity()));
            bonusPlanDetailParams = null;
            planDetailsResInfo = null;
            bonusPlanDetailList = null;

            int serial = 0;
            BigDecimal surplusBonus = reverseNumber;//??????????????????
            BigDecimal extendValidateNumber = BigDecimal.ZERO;//??????????????????????????????
            TblBonusDetail tblBonusDetailParam;
            Short operate = null;
            for (TblBonusDetail tblBonusDetail : tblBonusDetailList) { //????????????????????????
                if(surplusBonus.compareTo(BigDecimal.ZERO) == 0){ //??????????????????
                    break;
                }
                String validDate = tblBonusDetail.getValidDate();//??????????????????
                String bonusCdFlag = tblBonusDetail.getBonusCdFlag();//???????????????
                BigDecimal bonusDetailNumber = tblBonusDetail.getTxnBonus();//?????????????????????

                TblBonusPlanDetail planDetail = bonusPlanDetailMap.get(validDate);//???????????????????????????
                operate = "c".equals(bonusCdFlag) ? SysConstant.BONUS_OPERATE_INCREASE : SysConstant.BONUS_OPERATE_DECREASE;//????????????
                boolean isOverdue = DateUtil.isBefore4LocalDate(validDate, yearFlag, DateUtil.DATE_FORMAT_COMPACT);//?????????????????????????????????
                boolean isExtend = false;//????????????????????????????????????

                BigDecimal operateBonus;//??????????????????
                if(surplusBonus.compareTo(bonusDetailNumber) > 0){
                    operateBonus = bonusDetailNumber; //??????????????????>????????????????????????????????????????????????????????????????????????
                }else{
                    operateBonus = surplusBonus;
                }
                if(SysConstant.BONUS_OPERATE_INCREASE.equals(operate)){
                    if(isOverdue){
                        //?????????????????????
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
                //???????????????????????????
                planDetail.setModifyDate(hostDate);
                planDetail.setModifyTime(hostTime);
                this.tblBonusPlanDetailMapper.updatePlanDetailBonus(planDetail);

                if(!isExtend){
                    //????????????????????????????????????????????????????????????
                    ServiceBonusDetail serviceBonusDetail = new ServiceBonusDetail();
                    serviceBonusDetail.setOrderId(autoTblBonusReversal.getOrderId());
                    serviceBonusDetail.setOrderSerial(serial);
                    serviceBonusDetail.setOperate(operate);
                    serviceBonusDetail.setValidDate(validDate);
                    serviceBonusDetail.setNumber(operateBonus);
                    serviceBonusDetail.setReturnableNumber(operateBonus);
                    this.serviceBonusDetailMapper.insert(serviceBonusDetail);
                }

                //???????????????????????????
                tblBonusDetailParam = new TblBonusDetail();
                tblBonusDetailParam.setPkBonusDetail(tblBonusDetail.getPkBonusDetail());
                tblBonusDetailParam.setReturnFlag("1");
                this.tblBonusDetailMapper.updateByPrimaryKeySelective(tblBonusDetailParam);

                surplusBonus = surplusBonus.subtract(operateBonus);
                serial++;
            }
            tblBonusDetailParam = null;

            //????????????
            order.setOrderStatus("01");
            order.setReturnTime(hostDate);
            this.tblOrderMapper.updateStatus(order);

            //????????????????????????
            txnDetail.setReturnFlag("1");
            this.tblTxnDetailMapper.updateReturnFlag(txnDetail);

            //????????????
            this.autoExtension(extendValidateNumber,bonusPlanDetailMap,yearFlag,hostDate,hostTime,custId,orderId,serial);

            //????????????????????????
            bonusPlan.setModifyDate(hostDate);
            bonusPlan.setModifyTime(hostTime);
            this.tblBonusPlanMapper.updatePlanBonus(bonusPlan);

            //???????????????????????????
            serviceOrder.setValidAfter(bonusPlan.getValidBonus());//??????????????????
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
     * ????????????????????????
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
            log.info("??????[{}]??????????????????????????????[{}],?????????[{}]",custId,yearFlag,extendValidateNumber);
            TblBonusPlanDetail tblBonusPlanDetail = bonusPlanDetailMap.get(yearFlag);
            if(tblBonusPlanDetail != null){
                tblBonusPlanDetail.setTotalBonus(tblBonusPlanDetail.getTotalBonus().add(extendValidateNumber));
                tblBonusPlanDetail.setValidBonus(tblBonusPlanDetail.getValidBonus().add(extendValidateNumber));
                tblBonusPlanDetail.setModifyDate(hostDate);
                tblBonusPlanDetail.setModifyTime(hostTime);
                this.tblBonusPlanDetailMapper.updatePlanDetailBonus(tblBonusPlanDetail);
            }else{
                log.info("??????[{}]????????????????????????[{}],???????????????",custId,yearFlag);
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
            //?????????????????????????????????
            ServiceBonusDetail serviceBonusDetail = new ServiceBonusDetail();
            serviceBonusDetail.setOrderId(orderId);//???????????????id
            serviceBonusDetail.setOrderSerial(serial);
            serviceBonusDetail.setOperate(SysConstant.BONUS_OPERATE_INCREASE);
            serviceBonusDetail.setValidDate(yearFlag);
            serviceBonusDetail.setNumber(extendValidateNumber);
            this.serviceBonusDetailMapper.insert(serviceBonusDetail);
        }
    }

    /**
     * ????????????
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
                throw new BusinessException(getErrorInfo(SysConstant.SYS_LOCK_ERROR));//???????????????
            }

            String validate = DateUtil.getCurrentDateyyyy() + SysConstant.YEAR_FLAG; //??????????????????
            Date hostDate = adjustOrder.getHostDate();
            String hostDateStr = DateUtil.date2String(hostDate,DateUtil.DATE_FORMAT_COMPACT);//????????????
            String hostTimeStr = DateUtil.date2String(hostDate,DateUtil.DATE_FORMAT_TIME);//????????????

            /** ?????????????????? **/
            ResInfo planResInfo = queryMemberPointsRemote.queryBonusPlanByCustId(adjustOrder.getCustId(), SysConstant.BP_PLAN_TYPE_DEFAULT);
            TblBonusPlan bonusPlan = JacksonUtil.toObject(getResJson(planResInfo), TblBonusPlan.class);
            if(bonusPlan == null){
                log.info("??????[{}]???????????????????????????[{}]???TblBonusPlan?????????????????????",adjustOrder.getCustId(),SysConstant.DEFAULT_USAGE_KEY);
                bonusPlan = new TblBonusPlan();
                bonusPlan.setUsageKey(SysConstant.DEFAULT_USAGE_KEY);
                bonusPlan.setCustId(adjustOrder.getCustId());
                bonusPlan.setLockStatus(SysConstant.BONUS_PLAN_LOCK_STATUS_NORMAL);//??????????????????
                bonusPlan.setBpPlanType(SysConstant.BP_PLAN_TYPE_DEFAULT);//????????????
                bonusPlan.setTotalBonus(BigDecimal.ZERO);//???????????????
                bonusPlan.setValidBonus(BigDecimal.ZERO);//????????????
                bonusPlan.setApplyBonus(BigDecimal.ZERO);//????????????
                bonusPlan.setExpireBonus(BigDecimal.ZERO);//????????????
            }
            planResInfo = null;

            /** ????????????????????? **/
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
                log.info("??????[{}]???????????????????????????[{}]????????????[{}]???tblBonusPlanDetail?????????????????????",adjustOrder.getCustId(),SysConstant.BP_PLAN_TYPE_DEFAULT,validate);
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

            Short operate = SysConstant.BONUS_OPERATE_INCREASE;//????????????
            BigDecimal number = adjustOrder.getNumber();//????????????
            String adjustType = SysConstant.BONUS_FLAG_D;//????????????

            /** ?????????????????? **/
            ServiceOrder serviceOrderDto = new ServiceOrder();
            serviceOrderDto.setId(adjustOrder.getServiceOrderId());
            serviceOrderDto.setValidBefore(bonusPlan.getValidBonus());//???????????????
            this.operatePlanAndDetail(bonusPlan,currBonusPlanDetail,operate,number);
            serviceOrderDto.setValidAfter(bonusPlan.getValidBonus());//???????????????
            serviceOrderDto.setStatus(SysConstant.ORDER_STATUS_SUCCESS);

            /** ????????????????????? **/
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

            /** ??????CheckInf?????? **/
            TblCheckInf checkInf = new TblCheckInf();
            checkInf.setTeller1(SysConstant.DEFAULT_CREATE_OPER);//?????????
            checkInf.setTeller2(SysConstant.DEFAULT_CREATE_OPER);//?????????
            checkInf.setBpPlanType(SysConstant.BP_PLAN_TYPE_DEFAULT);
            checkInf.setCustId(adjustOrder.getCustId());
            checkInf.setAdjustType(adjustType);//????????????
            checkInf.setValidDate(validate);
            checkInf.setTxnBonus(number.longValue());//????????????
            checkInf.setTxnDesc(adjustOrder.getTxnDesc());//????????????
            checkInf.setOprFlag("1");//????????????
            checkInf.setCreateOper(SysConstant.DEFAULT_CREATE_OPER);//?????????
            checkInf.setCreateDate(hostDateStr);
            checkInf.setCreateTime(hostTimeStr);
            checkInf.setModifyOper(SysConstant.DEFAULT_CREATE_OPER);
            checkInf.setModifyDate(DateUtil.date2String(adjustOrder.getBusinessDate(),DateUtil.DATE_FORMAT_COMPACT));//???????????????
            checkInf.setModifyTime(hostTimeStr);
            checkInf.setBussId(currBonusPlanDetail.getPkBonusPlanDetail().toString());//???????????????
            checkInf.setBussType("02");//????????????
            checkInf.setTxnItems(adjustOrder.getTxnItems());//????????????
            checkInf.setStationId(adjustOrder.getStationId());
            checkInf.setBonusIntoType("0");//????????????
            checkInf.setAdjustProperty(adjustOrder.getAdjustProperty());//????????????
            checkInf.setAdjustOrderId(adjustOrder.getMallId());
            this.tblCheckInfMapper.insertSelective(checkInf);

            /** ?????????????????????????????? **/
            ServiceOrderDetail delayOrderDetail = new ServiceOrderDetail();
            delayOrderDetail.setId(checkInf.getTblCheckInf().toString());//??????????????????
            delayOrderDetail.setOrderId(adjustOrder.getServiceOrderId());
            delayOrderDetail.setGoodsId(String.valueOf(adjustOrder.getServiceOrderId()));//?????????id????????????id
            delayOrderDetail.setTotalPrice(number);
            delayOrderDetail.setUnitPrice(number);
            delayOrderDetail.setNumber(BigDecimal.ONE);
            delayOrderDetail.setReturnableNumber(BigDecimal.ONE);
            delayOrderDetail.setGoodsName(adjustOrder.getTxnDesc());
            delayOrderDetail.setGoodsType(SysConstant.GOODS_TYPE_REISSUE);
            this.serviceOrderDetailMapper.insert(delayOrderDetail);

            /** ???????????????????????? **/
            ServiceBonusDetail newServiceBonusDetail = new ServiceBonusDetail();
            newServiceBonusDetail.setOrderId(adjustOrder.getServiceOrderId());
            newServiceBonusDetail.setOrderSerial(0);
            newServiceBonusDetail.setOperate(operate);
            newServiceBonusDetail.setValidDate(validate);
            newServiceBonusDetail.setNumber(number);
            newServiceBonusDetail.setReturnableNumber(number);
            this.serviceBonusDetailMapper.insert(newServiceBonusDetail);

            /*??????????????????*/
            this.serviceOrderMapper.updateByPrimaryKeySelective(serviceOrderDto);

            /** ?????????????????? **/
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

            /** ???????????? **/
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
//                    log.error("???????????????????????????[{}]",e.getMessage());
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
