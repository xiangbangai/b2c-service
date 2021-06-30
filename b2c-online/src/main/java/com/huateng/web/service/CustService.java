package com.huateng.web.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pagehelper.PageInfo;
import com.huateng.base.BaseService;
import com.huateng.common.util.BusinessException;
import com.huateng.common.util.JacksonUtil;
import com.huateng.common.util.SysConstant;
import com.huateng.data.model.db1.TblCardUser;
import com.huateng.data.model.db2.*;
import com.huateng.data.vo.ResInfo;
import com.huateng.data.vo.params.QRCode;
import com.huateng.data.vo.params.TblCustLabelParams;
import com.huateng.service.remote.QueryCustRemote;
import com.huateng.service.remote.QueryMemberPointsRemote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2019/9/26
 * Time: 10:42
 * Description:
 */
@Slf4j
@Service
public class CustService extends BaseService {

    @Resource
    private QueryCustRemote queryCustRemote;
    @Resource
    private QueryMemberPointsRemote queryMemberPointsRemote;

    /**
     * @User Sam
     * @Date 2019/9/26
     * @Time 10:47
     * @Param
     * @return
     * @Description 校验二维码
     */
    public String checkQRCode(QRCode code) throws Exception {
        ResInfo resInfo = this.queryCustRemote.checkQRCode(code);
        if (!SysConstant.SYS_SUCCESS.equals(resInfo.getResCode())) {
            throw new BusinessException(getErrorForJson(resInfo.getResMsg()));
        } else {
            return resInfo.getResMsg();
        }
    }

    /**
     * 查询客户信息
     * @param id
     * @return
     */
    public TblCustInf queryCustInfo(String id) throws Exception {
        ResInfo resInfo = this.queryCustRemote.queryCustInfo(id);
        return JacksonUtil.toObject(getResJson(resInfo), TblCustInf.class);
    }

    /**
     * 根据会员号查询会员信息
     * @param custId
     * @return
     */
    public TblCustInf queryCustInfoByCustId(String custId) throws Exception {
        ResInfo resInfo = this.queryCustRemote.queryInfoById(custId);
        return JacksonUtil.toObject(getResJson(resInfo), TblCustInf.class);
    }

    /**
     * 查询会员和唯一属性
     * @param id
     * @return
     * @throws Exception
     */
    public TblCustInfAndAcct queryCustInfoAndAcct(String id) throws Exception {
        ResInfo resInfo = this.queryCustRemote.queryCustInfoAndAcct(id);
        return JacksonUtil.toObject(getResJson(resInfo), TblCustInfAndAcct.class);
    }

    /**
     * 查询会员信息
     * @param custId
     * @param integralType
     * @return
     */
    public TblBonusPlan pointsDetailList(String custId, String integralType) throws Exception {
        ResInfo resInfo = this.queryMemberPointsRemote.pointsDetailList(custId, integralType);
        return JacksonUtil.toObject(getResJson(resInfo), TblBonusPlan.class);
    }

    /**
     * 查询会员等级标签
     * @param tblCustLabelParams
     * @return
     * @throws Exception
     */
    public List<TblCustLabel> queryLevelLabelList(TblCustLabelParams tblCustLabelParams) throws Exception {
        ResInfo resInfo = this.queryCustRemote.queryLevelLabelList(tblCustLabelParams);
        return JacksonUtil.toObject(getResJson(resInfo),  new TypeReference<List<TblCustLabel>>(){});
    }

    /**
     * 查询车辆信息
     * @param plateNumber
     * @return
     */
    public List<TblUserCar> queryUserCarsByPlateNumber(String plateNumber) throws Exception {
        ResInfo resInfo = this.queryCustRemote.queryUserCarsByPlateNumber(plateNumber);
        return JacksonUtil.toObject(getResJson(resInfo),  new TypeReference<List<TblUserCar>>(){});
    }

    /**
     * 查询会员用户信息
     * @param custId
     * @return
     */
    public List<TblCustUser> queryTblCustUsersByCustId(String custId) throws Exception {
        ResInfo resInfo = this.queryCustRemote.queryTblCustUsersByCustId(custId);
        return JacksonUtil.toObject(getResJson(resInfo),  new TypeReference<List<TblCustUser>>(){});
    }

    /**
     * 根据custId和acctType获取卡号
     * @param custId
     * @param acctType
     * @return
     */
    public TblAcctInf queryTblAcctInfByCustId(String custId, String acctType) throws Exception {
        ResInfo resInfo = this.queryCustRemote.queryTblAcctInfByCustId(custId, acctType);
        return JacksonUtil.toObject(getResJson(resInfo),  TblAcctInf.class);
    }

}
