package com.huateng.service.remote;

import com.huateng.data.vo.ResInfo;
import com.huateng.data.vo.params.BonusPlanDetailParams;
import com.huateng.data.vo.params.MemberPointsTxnParams;
import com.huateng.data.vo.params.OrderInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/3/6
 * Time: 22:29
 * Description:
 */
@FeignClient(value = "b2c-service-query", contextId = "queryMemberPointsRemote", path = "/query/memberPoints")
public interface QueryMemberPointsRemote {

    /**
     * 获取会员积分信息
     * @param custId
     * @param integralType
     * @return
     */
    @PostMapping("/pointsDetailList")
    ResInfo pointsDetailList(@RequestParam String custId, @RequestParam String integralType);

    /**
     * 查询订单
     * @param orderInfo
     * @return
     */
    @PostMapping("/queryOrderInfo")
    ResInfo queryOrderInfo(@RequestBody OrderInfo orderInfo);

    /**
     * 查询老交易订单
     * @param acqSsn
     * @param acctId
     * @return
     */
    @PostMapping("/queryTblOrderInfo")
    ResInfo queryTblOrderInfo(@RequestParam String acqSsn,@RequestParam String acctId);


    /**
     * 查询积分流水信息
     * @param memberPointsTxnParams
     * @return
     */
    @PostMapping("/queryTxnInfoPageList")
    ResInfo queryTxnInfo(@RequestBody MemberPointsTxnParams memberPointsTxnParams);

    /**
     *  查询会员可用积分明细
     * @param bonusPlanDetailParams
     * @return
     */
    @PostMapping("/bonusPlanDetail")
    ResInfo queryBonusPlanDetail(@RequestBody BonusPlanDetailParams bonusPlanDetailParams);

    /**
     * 查询商品信息
     * @param orderId
     * @return
     * @throws Exception
     */
    @PostMapping("/queryNewOrderDetailByOrderId")
    ResInfo queryNewOrderDetailByOrderId(@RequestParam String orderId);
}
