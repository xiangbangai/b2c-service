package com.huateng.web.controller;

import com.huateng.base.BaseController;
import com.huateng.common.util.BusinessException;
import com.huateng.common.util.DateUtil;
import com.huateng.common.util.JacksonUtil;
import com.huateng.common.util.SysConstant;
import com.huateng.config.apollo.ApolloBean;
import com.huateng.data.model.db2.TblCustInf;
import com.huateng.data.model.db2.TblCustInfAndAcct;
import com.huateng.data.model.db2.TblCustUser;
import com.huateng.data.model.db2.TblLabelGroup;
import com.huateng.data.vo.ResInfo;
import com.huateng.data.vo.params.LabelGroupInfo;
import com.huateng.data.vo.params.LabelParam;
import com.huateng.data.vo.params.QRCode;
import com.huateng.data.vo.params.TblCustLabelParams;
import com.huateng.web.service.CustService;
import com.huateng.web.service.RedisService;
import com.huateng.web.service.ValidateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2019/9/12
 * Time: 16:38
 * Description:
 */
@Slf4j
@Api(tags = "会员相关查询服务")
@RestController
public class CustController extends BaseController {

    @Resource
    private RedisService redisService;
    @Resource
    private ApolloBean apolloBean;
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
     * @Description 验证二维码是否可用
     */
    @ApiOperation(value = "校验二维码", notes = "如果可用返回用户会员号")
    @ApiImplicitParam(name = "qrCode", value = "二维码参数", required = true, dataType = "QRCode")
    @PostMapping("/cust/checkQRCode")
    public ResInfo checkQRCode(@RequestBody QRCode qrCode) throws Exception {
        ResInfo resInfo = new ResInfo();

        /**校验参数**/
        this.validateService.validateForObject(qrCode, SysConstant.E90000005);
        this.validateService.validateForString(qrCode.getCode(), SysConstant.E90000005);

        /**检查是否过期**/
        String acctId = this.custService.checkTimeout(qrCode);

        if (qrCode.getIsCheckRepeat()){
            //redis判断是否重复使用
            if (!this.redisService.setIfAbsent(SysConstant.REDIS_KEY_QRCODE.concat(qrCode.getCode()), qrCode.getCode(),
                    this.apolloBean.getWechatTimeOut(), TimeUnit.SECONDS)) {
                //二维码重复使用
                throw new BusinessException(getErrorInfo(SysConstant.E90000002));
            }
        }
        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        resInfo.setResMsg(acctId);
        return resInfo;
    }

    /**
     * 查询客户信息
     * @param id
     * @return
     */
    @ApiOperation(value = "查询会员信息", notes = "返回会员信息对象")
    @ApiImplicitParam(name = "id", value = "会员唯一属性", required = true, dataType = "String")
    @PostMapping("/cust/info")
    public ResInfo custInfo(String id) throws Exception {
        ResInfo resInfo = new ResInfo();


        /**校验参数**/
        this.validateService.validateForString(id, SysConstant.E90000006);


        TblCustInf tblCustInf = null;
        try {
            tblCustInf = this.custService.queryCustInfo(id);
        } catch (MyBatisSystemException e) {
            if(e.contains(TooManyResultsException.class)){
                log.error("查询会员信息，返回记录不止一条[{}]",id);
                throw new BusinessException(getErrorInfo(SysConstant.E90000004));
            }else{
                throw e;
            }
        }

        resInfo.setData(JacksonUtil.toJson(tblCustInf));
        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }


    /**
     * 查询客户以及唯一属性
     * @param id
     * @return
     */
    @ApiOperation(value = "查询会员和唯一属性", notes = "返回会员和唯一属对象")
    @ApiImplicitParam(name = "id", value = "会员唯一属性", required = true, dataType = "String")
    @PostMapping("/cust/infoAndAcct")
    public ResInfo infoAndAcct(String id) throws Exception {
        ResInfo resInfo = new ResInfo();

        /**校验参数**/
        this.validateService.validateForString(id, SysConstant.E90000006);


        TblCustInfAndAcct tblCustInfAndAcct = null;
        try {
            tblCustInfAndAcct = this.custService.queryCustInfoAndAcct(id);
        } catch (MyBatisSystemException e) {
            if(e.contains(TooManyResultsException.class)){
                log.error("查询会员和唯一属性，返回记录不止一条[{}]",id);
                throw new BusinessException(getErrorInfo(SysConstant.E90000004));
            }else{
                throw e;
            }
        }

        resInfo.setData(JacksonUtil.toJson(tblCustInfAndAcct));
        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }


    @ApiOperation(value = "查询会员标签", notes = "返回标签集合")
    @ApiImplicitParam(name = "labelParam", value = "会员标签参数", required = true, dataType = "LabelParam")
    @PostMapping("/cust/custLabel")
    public ResInfo queryCustLabel(@RequestBody LabelParam labelParam)throws Exception{
        ResInfo resInfo = new ResInfo();


        List<String> list = this.custService.queryCustLabel(labelParam.getCustId(), DateUtil.date2String(labelParam.getCurDate(),DateUtil.DATE_FORMAT_COMPACT));

        if(!list.isEmpty()){
            resInfo.setData(JacksonUtil.toJson(list));
        }
        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }

    @ApiOperation(value = "查询会员标签标签组", notes = "返回标签组集合")
    @ApiImplicitParam(name = "labelGroupInfo", value = "标签组参数", required = true, dataType = "LabelGroupInfo")
    @PostMapping("/cust/custLabelGroup")
    public ResInfo custLabelGroup(@RequestBody LabelGroupInfo labelGroupInfo)throws Exception{
        ResInfo resInfo = new ResInfo();


        List<TblLabelGroup> list = this.custService.custLabelGroup(labelGroupInfo);

        if(!list.isEmpty()){
            resInfo.setData(JacksonUtil.toJson(list));
        }
        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }

    @ApiOperation(value = "根据会员id查询会员信息", notes = "返回会员对象")
    @ApiImplicitParam(name = "custId", value = "会员id", required = true, dataType = "String")
    @PostMapping("/cust/queryInfoById")
    public ResInfo queryInfoById(String custId) throws Exception {
        ResInfo resInfo = new ResInfo();


        /**校验参数**/
        this.validateService.validateForString(custId, SysConstant.E90000007);

        TblCustInf tblCustInf = this.custService.queryInfoById(custId);

        resInfo.setData(JacksonUtil.toJson(tblCustInf));
        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }


    @ApiOperation(value = "查询会员等级标签", notes = "返回等级标签集合")
    @ApiImplicitParam(name = "tblCustLabelParams", value = "标签参数", required = true, dataType = "TblCustLabelParams")
    @PostMapping("/cust/levelLabelList")
    public ResInfo queryCustLabelList(@RequestBody TblCustLabelParams tblCustLabelParams)throws Exception{
        ResInfo resInfo = new ResInfo();

        resInfo.setData(JacksonUtil.toJson(this.custService.queryCustLabelInfo(tblCustLabelParams)));
        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }

    @ApiOperation(value = "根据会员id查询会员基础信息", notes = "返回会员基础对象")
    @ApiImplicitParam(name = "custId", value = "会员id", required = true, dataType = "String")
    @PostMapping("/cust/custUser")
    public ResInfo queryCustUser(String custId) throws Exception {
        ResInfo resInfo = new ResInfo();

        /**校验参数**/
        this.validateService.validateForString(custId, SysConstant.E90000007);

        TblCustUser tblCustUser = this.custService.queryCustUser(custId);

        resInfo.setData(JacksonUtil.toJson(tblCustUser));
        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }

    @ApiOperation(value = "查询车辆信息", notes = "返回车辆信息")
    @ApiImplicitParam(name = "plateNumber", value = "车牌号", required = true, dataType = "String")
    @PostMapping("/cust/userCarsByPlateNumber")
    public ResInfo queryUserCarsByPlateNumber(@RequestParam String plateNumber)throws Exception{
        ResInfo resInfo = new ResInfo();

        /**校验参数**/
        this.validateService.validateForString(plateNumber, SysConstant.E90000013);

        resInfo.setData(JacksonUtil.toJson(this.custService.queryUserCarsByPlateNumber(plateNumber)));
        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }

    @ApiOperation(value = "查询会员用户信息", notes = "返回会员用户信息")
    @ApiImplicitParam(name = "custId", value = "会员ID", required = true, dataType = "String")
    @PostMapping("/cust/tblCustUsersByCustId")
    public ResInfo queryTblCustUsersByCustId(@RequestParam String custId)throws Exception{
        ResInfo resInfo = new ResInfo();

        /**校验参数**/
        this.validateService.validateForString(custId, SysConstant.E90000007);

        resInfo.setData(JacksonUtil.toJson(this.custService.queryTblCustUsersByCustId(custId)));
        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }

    @ApiOperation(value = "根据custId和acctType获取卡号", notes = "返回支付卡信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "custId", value = "会员ID", required = true, dataType = "String"),
            @ApiImplicitParam(name = "acctType", value = "账号类型", required = true, dataType = "String")
    })
    @PostMapping("/cust/tblAcctInfByCustId")
    public ResInfo queryTblAcctInfByCustId(@RequestParam String custId, @RequestParam String acctType)throws Exception{
        ResInfo resInfo = new ResInfo();

        /**校验参数**/
        this.validateService.validateForString(custId, SysConstant.E90000007);
        this.validateService.validateForString(acctType, SysConstant.E90000012);

        resInfo.setData(JacksonUtil.toJson(this.custService.queryTblAcctInfByCustId(custId, acctType)));
        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }
}