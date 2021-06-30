package com.huateng.service.remote;

import com.huateng.data.model.db2.*;
import com.huateng.data.vo.ResInfo;
import com.huateng.data.vo.params.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.List;

@FeignClient(value = "b2c-member-points", contextId = "memberPointsRemote", path = "/memberPoints/operate")
public interface MemberPointsRemote {

    /**
     * 如积分冲正类 只保存主订单的交易
     * 保存主订单
     * @param order
     * @return
     */
    @PostMapping("/saveMainOrder")
    ResInfo saveMainOrder(@RequestBody ServiceOrder order);

    /**
     * 积分兑换
     * @param id
     * @param exchangeGoods
     * @param custId
     * @param date
     * @return
     */
    @PostMapping("/exchange")
    ResInfo exchange(@RequestParam String id, @RequestParam String custId, @RequestParam Date date, @RequestBody List<ExchangeGoods> exchangeGoods);

    /**
     * 计算积分
     * @param computeBonus
     * @return
     */
    @PostMapping("/computeBonus")
    ResInfo computeBonus(@RequestBody ComputeBonus computeBonus);

    /**
     * 积分入账
     * @param bonusEnter
     * @return
     */
    @PostMapping("/bonusPlanEnter")
    ResInfo bonusPlanEnter(@RequestBody BonusEnter bonusEnter);

    /**
     * 积分冲正
     * @param autoBonusReversal
     * @return
     */
    @PostMapping("/bonusReversal")
    ResInfo bonusReversal(@RequestBody AutoBonusReversal autoBonusReversal);

    /**
     * 积分冲正
     * @param tblBonusReversal
     * @return
     */
    @PostMapping("/tblBonusReversal")
    ResInfo tblBonusReversal(@RequestBody AutoTblBonusReversal tblBonusReversal);

    /**
     * 积分调整
     * @param adjustOrder
     * @return
     */
    @PostMapping("/adjust")
    ResInfo adjust(AdjustOrder adjustOrder);
}
