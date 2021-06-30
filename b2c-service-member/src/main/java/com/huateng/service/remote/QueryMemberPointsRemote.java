package com.huateng.service.remote;

import com.huateng.data.vo.ResInfo;
import com.huateng.data.vo.params.BonusPlanDetailParams;
import com.huateng.data.vo.params.OrderInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "b2c-service-query", contextId = "queryMemberPointsRemote", path = "/query/memberPoints")
public interface QueryMemberPointsRemote {

    /**
     * 查询客户补发
     * @return
     */
    @PostMapping("/bonusDelay")
    ResInfo queryCustBonusDelay(@RequestParam String custId);

    /**
     *  查询会员积分计划
     * @param custId
     * @param bpPlanType
     * @return
     */
    @PostMapping("/bonusPlan")
    ResInfo queryBonusPlanByCustId(@RequestParam String custId, @RequestParam String bpPlanType);

    /**
     *  查询会员可用积分明细
     * @param bonusPlanDetailParams
     * @return
     */
    @PostMapping("/bonusPlanDetail")
    ResInfo queryBonusPlanDetail(@RequestBody BonusPlanDetailParams bonusPlanDetailParams);

    /**
     * 查询订单
     * @param id
     * @return
     * @throws Exception
     */
    @PostMapping("/queryOrderById")
    ResInfo queryOrderById(@RequestParam String id);

    /**
     * 查询商品信息
     * @param orderId
     * @return
     * @throws Exception
     */
    @PostMapping("/queryNewOrderDetailByOrderId")
    ResInfo queryNewOrderDetailByOrderId(@RequestParam String orderId);

    /**
     * 查询流水信息
     * @param orderId
     * @return
     * @throws Exception
     */
    @PostMapping("/queryNewBonusDetailByOrderId")
    ResInfo queryNewBonusDetailByOrderId(@RequestParam String orderId);

    /**
     * 查询订单信息
     * @param orderInfo
     * @return
     */
    @PostMapping("/queryOrderInfo")
    ResInfo queryOrderInfo(@RequestBody OrderInfo orderInfo);

    /**
     * 根据有效期查询客户积分有效期
     * @param custId
     * @param bpPlanType
     * @param validates
     * @return
     */
    @PostMapping("/queryBonusPlanDetails")
    ResInfo queryBonusPlanDetails(@RequestParam String custId, @RequestParam String bpPlanType, @RequestBody List<String> validates);

    /**
     * 老交易-积分冲正
     * 查询客户已冲正积分
     * @param custId
     * @param bonusSsnOra
     * @return
     */
    @PostMapping("/queryCustReversalBonus")
    ResInfo queryCustReversalBonus(@RequestParam String custId, @RequestParam String bonusSsnOra);

    /**
     * 查询老交易订单
     * @param orderId
     * @return
     */
    @PostMapping("/queryTblOrderByOrderId")
    ResInfo queryTblOrderByOrderId(@RequestParam String orderId);

    /**
     * 查询交易流水
     * @param acqSsn
     * @return
     */
    @PostMapping("/queryTxnDetailByAcqSsn")
    ResInfo queryTxnDetailByAcqSsn(@RequestParam String acqSsn);

    /**
     * 查询订单商品
     * @param orderId
     * @return
     */
    @PostMapping("/queryOrderDetailByOrderId")
    ResInfo queryOrderDetailByOrderId(@RequestParam String orderId);

    /**
     * 查询积分流水
     * @param bonusSsn
     * @return
     */
    @PostMapping("/queryBonusDetailByBonusSsn")
    ResInfo queryBonusDetailByBonusSsn(@RequestParam String bonusSsn);
}
