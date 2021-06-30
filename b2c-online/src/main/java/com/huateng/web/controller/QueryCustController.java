package com.huateng.web.controller;

import cn.hutool.core.util.NumberUtil;
import com.huateng.base.BaseController;
import com.huateng.common.util.BusinessException;
import com.huateng.common.util.DateUtil;
import com.huateng.common.util.SysConstant;
import com.huateng.data.model.db2.*;
import com.huateng.data.vo.json.Request;
import com.huateng.data.vo.json.Response;
import com.huateng.data.vo.json.req.QueryLevelLabel;
import com.huateng.data.vo.json.req.QueryMemberInfo;
import com.huateng.data.vo.json.res.LevelLabelInfo;
import com.huateng.data.vo.json.res.ResCustAndUserCarInfo;
import com.huateng.data.vo.json.res.ResLevelLabel;
import com.huateng.data.vo.json.res.ResMemberInfo;
import com.huateng.data.vo.params.QRCode;
import com.huateng.data.vo.params.TblCustLabelParams;
import com.huateng.data.vo.params.UserCarParams;
import com.huateng.web.service.CustService;
import com.huateng.web.service.ValidateService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2019/9/12
 * Time: 15:48
 * Description:积分账户查询
 */
@Slf4j
@RestController
public class QueryCustController extends BaseController {

    @Resource
    private CustService custService;
    @Resource
    private ValidateService validateService;

    /**
     * @User Sam
     * @Date 2019/9/25
     * @Time 22:25
     * @Param
     * @return
     * @Description 查询会员信息
     */
    @PostMapping("/query/cust/memberInfo")
    public Response queryMemberInfo() throws Exception {
        Response response = new Response();
        Request request;
        QueryMemberInfo queryMemberInfo;
        String channel;
        request = getObject(QueryMemberInfo.class);
        queryMemberInfo = (QueryMemberInfo) request.getData();
        channel = request.getChannel();
        /**验证参数**/
        this.validateService.validateForString(queryMemberInfo.getAcctId(), SysConstant.E01000003);

        /**二维码中的会员号，或报文中的acctId**/
        String acctId = "";
        /**二维码**/
        if(queryMemberInfo.getAcctId().length() == 39) {
            QRCode code = new QRCode();
            code.setCode(queryMemberInfo.getAcctId());
            code.setIsCheckTime(true);
            code.setIsCheckRepeat(true);
            acctId = this.custService.checkQRCode(code);
        }else{
            /**不是二维码，直接查询**/
            acctId = queryMemberInfo.getAcctId();
        }

        /**不允许用手机查询**/
        validateChannelForMobile(queryMemberInfo.getAcctId(), channel);

        /**查询会员信息**/
        TblCustInfAndAcct tblCustInfAndAcct = this.custService.queryCustInfoAndAcct(acctId);
        if (tblCustInfAndAcct == null) {
            throw new BusinessException(getErrorInfo(SysConstant.E01000036));
        }
        /**POS渠道不允许使用会员查询**/
        validateChannelForParams(tblCustInfAndAcct.getCustId(), queryMemberInfo.getAcctId(), channel, SysConstant.E01000050); //当前渠道不允许会员号查询会员信息

        /**查询会员积分**/
        TblBonusPlan tblBonusPlan = this.custService.pointsDetailList(tblCustInfAndAcct.getCustId(), SysConstant.BP_PLAN_TYPE_DEFAULT);

        ResMemberInfo resMemberInfo = new ResMemberInfo();
        resMemberInfo.setCustId(tblCustInfAndAcct.getCustId());
        resMemberInfo.setMobile(tblCustInfAndAcct.getCustMobile() == null ? "" : tblCustInfAndAcct.getCustMobile());
        resMemberInfo.setCustInvoice(tblCustInfAndAcct.getCustInvoice());
        resMemberInfo.setAcctList(tblCustInfAndAcct.getAcctList());

        if (tblBonusPlan != null) {
            resMemberInfo.setBpPlanType(tblBonusPlan.getBpPlanType());
            resMemberInfo.setBpPlanTypeName(SysConstant.INTEGRAL_NAME);
            resMemberInfo.setTotalBonus(tblBonusPlan.getTotalBonus());
            resMemberInfo.setApplyBonus(tblBonusPlan.getApplyBonus());
            resMemberInfo.setExpiredBonus(tblBonusPlan.getExpireBonus());
            resMemberInfo.setValidBonus(tblBonusPlan.getValidBonus());
            resMemberInfo.setCustBonusStatus(tblBonusPlan.getLockStatus());
            resMemberInfo.setExpiredBonusThisYear(NumberUtil.null2Zero(tblBonusPlan.getValidBonus2()));
        }else{
            //兼容客户端
            BigDecimal zero = BigDecimal.ZERO;
            resMemberInfo.setBpPlanType(SysConstant.BP_PLAN_TYPE_DEFAULT);
            resMemberInfo.setBpPlanTypeName(SysConstant.INTEGRAL_NAME);
            resMemberInfo.setTotalBonus(zero);
            resMemberInfo.setApplyBonus(zero);
            resMemberInfo.setExpiredBonus(zero);
            resMemberInfo.setValidBonus(zero);
            resMemberInfo.setCustBonusStatus("0");//积分账户正常
            resMemberInfo.setExpiredBonusThisYear(zero);
        }
        response.setData(resMemberInfo);

        response.setResCode(SysConstant.SYS_SUCCESS);

        return response;
    }

    /**
     * 查询会员等级标签
     * @return
     */
    @PostMapping("/query/cust/levelLabelList")
    public Response queryLevelLabelList() throws Exception {
        Response response = new Response();
        Request request;
        QueryLevelLabel queryLevelLabel;
        request = getObject(QueryLevelLabel.class);
        queryLevelLabel = (QueryLevelLabel) request.getData();

        /**验证参数**/
        this.validateService.validateForString(queryLevelLabel.getAcctId(), SysConstant.E01000003); //acctId不可为空

        TblCustInf tblCustInf = this.custService.queryCustInfo(queryLevelLabel.getAcctId());
        if (tblCustInf == null) {
            throw new BusinessException(getErrorInfo(SysConstant.E01000036)); //会员不存在
        }

        TblCustLabelParams tblCustLabelParams = new TblCustLabelParams();
        tblCustLabelParams.setCustId(tblCustInf.getCustId());
        tblCustLabelParams.setLabelType(SysConstant.CUST_LABEL_TYPE_2);
        tblCustLabelParams.setStatus(SysConstant.CUST_LABEL_STATUS_01);
        tblCustLabelParams.setDateBetween(DateUtil.getCurrentDateyyyyMMdd());

        /**获取等级标签列表**/
        List<TblCustLabel> list = this.custService.queryLevelLabelList(tblCustLabelParams);

        //返回对象
        ResLevelLabel resLevelLabel = new ResLevelLabel();
        resLevelLabel.setCustId(tblCustInf.getCustId());

        /**组装返回对象**/
        List<LevelLabelInfo> resultList = new ArrayList<>();
        LevelLabelInfo levelLabelInfo;
        for (TblCustLabel t : list) {
            levelLabelInfo = new LevelLabelInfo();
            BeanUtils.copyProperties(t, levelLabelInfo);
            resultList.add(levelLabelInfo);
        }


        resLevelLabel.setList(resultList);
        response.setData(resLevelLabel);
        response.setResCode(SysConstant.SYS_SUCCESS);
        return response;
    }

    /**
     * 根据车牌号查询会员信息和无感支付信息（3141）
     * @return
     */
    @PostMapping("/query/cust/queryCustByPlateNumber")
    public Response queryCustByPlateNumber() throws Exception {
        Response response = new Response();
        Request request;
        UserCarParams userCarParams;
        request = getObject(UserCarParams.class);
        userCarParams = (UserCarParams) request.getData();

        /**验证参数**/
        this.validateService.validateForString(userCarParams.getPlateNumber(), SysConstant.E01000072); //车牌号不可为空

        //查询车辆信息
        List<TblUserCar> tblUserCarList = this.custService.queryUserCarsByPlateNumber(userCarParams.getPlateNumber());
        if (tblUserCarList == null || tblUserCarList.size() <= 0) {
            throw new BusinessException(getErrorInfo(SysConstant.E01000070)); //车辆不存在
        }

        TblUserCar tblUserCar = tblUserCarList.get(0);
        //查询会员信息
        TblCustInf custInf = this.custService.queryCustInfoByCustId(tblUserCar.getCustId());

        if(custInf == null) {
            throw new BusinessException(getErrorInfo(SysConstant.E01000036)); //会员不存在
        }

        String custId = custInf.getCustId();
        List<TblCustUser> tblCustUserList = this.custService.queryTblCustUsersByCustId(custId);
        TblCustUser tblCustUser = null;
        if(!tblCustUserList.isEmpty()) {
            tblCustUser = tblCustUserList.get(0);
        }

        TblAcctInf tblAcctInf = this.custService.queryTblAcctInfByCustId(custId, "92");

        ResCustAndUserCarInfo resCustAndUserCarInfo = new ResCustAndUserCarInfo();
        resCustAndUserCarInfo.setAcctId(custId);
        resCustAndUserCarInfo.setOpenId(custInf.getOpenId());

        if(tblCustUser != null) {
            resCustAndUserCarInfo.setAcctName(tblCustUser.getCustName());
        } else {
            resCustAndUserCarInfo.setAcctName("");
        }
        if(tblAcctInf != null) {
            resCustAndUserCarInfo.setUid(tblAcctInf.getAcctId());
        } else {
            resCustAndUserCarInfo.setUid("");
        }
        String nonInductivePayType = tblUserCar.getNonInductivePayType();
        if(StringUtils.isNotBlank(nonInductivePayType)) {
            resCustAndUserCarInfo.setNonInductivePayType(nonInductivePayType);
        } else {
            resCustAndUserCarInfo.setNonInductivePayType("");
        }
        String defaultNonInductivePayType = tblUserCar.getDefaultNonInductivePayType();
        if(StringUtils.isNotBlank(defaultNonInductivePayType)) {
            resCustAndUserCarInfo.setDefaultNonInductivePayType(defaultNonInductivePayType);
        } else {
            resCustAndUserCarInfo.setDefaultNonInductivePayType("");
        }
        Integer oilType = tblUserCar.getOilType();
        if(oilType != null) {
            resCustAndUserCarInfo.setOilType(oilType + "");
        } else {
            resCustAndUserCarInfo.setOilType("");
        }
        String licensePlateType = tblUserCar.getLicensePlateType();
        if(StringUtils.isNotBlank(licensePlateType)) {
            resCustAndUserCarInfo.setLicensePlateType(licensePlateType);
        } else {
            resCustAndUserCarInfo.setLicensePlateType("");
        }

        response.setData(resCustAndUserCarInfo);
        response.setResCode(SysConstant.SYS_SUCCESS);
        return response;
    }
}
