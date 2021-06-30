package com.huateng.web.service;

import com.huateng.base.BaseService;
import com.huateng.common.util.BusinessException;
import com.huateng.common.util.CaesarCrypt;
import com.huateng.common.util.SysConstant;
import com.huateng.config.apollo.ApolloBean;
import com.huateng.data.db2.mapper.*;
import com.huateng.data.model.db2.*;
import com.huateng.data.vo.params.LabelGroupInfo;
import com.huateng.data.vo.params.QRCode;
import com.huateng.data.vo.params.TblCustLabelParams;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2019/9/14
 * Time: 20:53
 * Description:
 */
@Slf4j
@Service
public class CustService extends BaseService {

    @Resource
    private ApolloBean apolloBean;
    @Resource
    private TblCustInfMapper tblCustInfMapper;
    @Resource
    private TblCustLabelMapper tblCustLabelMapper;
    @Resource
    private TblCustUserMapper tblCustUserMapper;
    @Resource
    private TblLabelGroupMapper tblLabelGroupMapper;
    @Resource
    private TblAcctInfMapper tblAcctInfMapper;
    @Resource
    private TblUserCarMapper tblUserCarMapper;



    /**
     * 判断二维码是否过期
     * @param qrCode
     * @return
     * @throws Exception
     */
    public String checkTimeout(QRCode qrCode) throws Exception {
        //获取报文头
        CaesarCrypt caesar = new CaesarCrypt("0123456789");
        //获取密文
        String encodeCarid = StringUtils.substring(qrCode.getCode(), 0, 19);
        String encodeTimestamp = StringUtils.substring(qrCode.getCode(), 19, 31);
        String encodeRandom = StringUtils.substring(qrCode.getCode(), 31, 40);
        //解密随机数
        int decodeRandom = new Integer(caesar.decrypt(encodeRandom, -this.apolloBean.getWechatKey()));
        //解密密文、时间戳
        String acctId  = caesar.decrypt(encodeCarid, -decodeRandom).replaceFirst("^0+","");

        if(qrCode.getIsCheckTime()){
            int starTime = Integer.parseInt(caesar.decrypt(encodeTimestamp, -decodeRandom).replaceFirst("^0+",""));
            //判断时间戳是否超过
            int nowTime =  Integer.parseInt(Long.toString(new Date().getTime() / 1000));
            if (nowTime - starTime > this.apolloBean.getWechatTimeOut()) {
                //二维码过期
                throw new BusinessException(getErrorInfo(SysConstant.E90000001));
            }
        }
        return acctId;
    }

    /**
     * @User Sam
     * @Date 2019/9/26
     * @Time 11:55
     * @Param
     * @return
     * @Description 查询会员信息
     */
    public TblCustInf queryCustInfo(String id) {
        return this.tblCustInfMapper.getCustInfo(id);
    }

    /**
     * 根据会员号查询会员信息
     * @param custId
     * @return
     */
    public TblCustInf queryInfoById(String custId) {
        return this.tblCustInfMapper.queryInfoById(custId);
    }

    /**
     * 查询会员标签
     * @param custId
     * @return
     */
    public List<String> queryCustLabel(String custId,String expDate) {
        return tblCustLabelMapper.queryCustLabel(custId,expDate);
    }

    /**
     * 查询会员等级标签
     * @param tblCustLabelParams
     * @return
     */
    public List<TblCustLabel> queryCustLabelInfo(TblCustLabelParams tblCustLabelParams) {
        return this.tblCustLabelMapper.getInfo(tblCustLabelParams);
    }

    /**
     * 查询会员信息和唯一属性
     * @param custId
     * @return
     */
    public TblCustInfAndAcct queryCustInfoAndAcct(String custId) {
        return this.tblCustInfMapper.getInfoAndAcct(custId);
    }

    /**
     * 查询会员基础信息信息和唯一属性
     * @param custId
     * @return
     */
    public TblCustUser queryCustUser(String custId) {
        return this.tblCustUserMapper.queryCustUser(custId);
    }

    public List<TblLabelGroup> custLabelGroup(LabelGroupInfo labelGroupInfo) {
        return this.tblLabelGroupMapper.queryLabelGroupByLabelGroupInfo(labelGroupInfo);
    }

    /**
     * 查询车辆信息
     * @param plateNumber
     * @return
     */
    public List<TblUserCar> queryUserCarsByPlateNumber(String plateNumber) {
        return this.tblUserCarMapper.queryUserCarsByPlateNumber(plateNumber);
    }

    /**
     * 查询会员用户信息
     * @param custId
     * @return
     */
    public List<TblCustUser> queryTblCustUsersByCustId(String custId) {
        return this.tblCustUserMapper.queryTblCustUsersByCustId(custId);
    }

    /**
     * 根据custId和acctType获取卡号
     * @param custId
     * @param acctType
     * @return
     */
    public TblAcctInf queryTblAcctInfByCustId(String custId, String acctType) {
        return this.tblAcctInfMapper.queryTblAcctInfByCustId(custId, acctType);
    }
}
