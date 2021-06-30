package com.huateng.service.remote;

import com.huateng.data.vo.ResInfo;
import com.huateng.data.vo.params.QRCode;
import com.huateng.data.vo.params.TblCustLabelParams;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/3/6
 * Time: 22:18
 * Description:
 */
@FeignClient(value = "b2c-service-query", contextId = "queryCustRemote", path = "/query/cust")
public interface QueryCustRemote {

    /**
     * @User Sam
     * @Date 2019/9/26
     * @Time 13:39
     * @Param
     * @return
     * @Description 验证二维码
     */
    @PostMapping("/checkQRCode")
    ResInfo checkQRCode(@RequestBody QRCode qrCode);

    /**
     * 查询客户积分信息
     * @param id 客户di
     * @return
     */
    @PostMapping("/info")
    ResInfo queryCustInfo(@RequestParam String id);

    /**
     * 查询会员和唯一属性
     * @param id
     * @return
     */
    @PostMapping("/infoAndAcct")
    ResInfo queryCustInfoAndAcct(@RequestParam String id);


    /**
     * 根据会员号查询会员信息
     * @param custId
     * @return
     */
    @PostMapping("/queryInfoById")
    ResInfo queryInfoById(@RequestParam String custId);


    /**
     * 查询会员等级标签
     * @param tblCustLabelParams
     * @return
     */
    @PostMapping("/levelLabelList")
    ResInfo queryLevelLabelList(@RequestBody TblCustLabelParams tblCustLabelParams);

    /**
     * 查询车辆信息
     * @param plateNumber
     * @return
     */
    @PostMapping("/userCarsByPlateNumber")
    ResInfo queryUserCarsByPlateNumber(@RequestParam String plateNumber);


    /**
     * 查询会员用户信息
     * @param custId
     * @return
     */
    @PostMapping("/tblCustUsersByCustId")
    ResInfo queryTblCustUsersByCustId(@RequestParam String custId);


    /**
     * 根据custId和acctType获取卡号
     * @param custId
     * @param acctType
     * @return
     */
    @PostMapping("/tblAcctInfByCustId")
    ResInfo queryTblAcctInfByCustId(@RequestParam String custId, @RequestParam String acctType);
}
