package com.huateng.web.controller;

import cn.hutool.core.collection.CollUtil;
import com.huateng.base.BaseController;
import com.huateng.common.util.BusinessException;
import com.huateng.common.util.JacksonUtil;
import com.huateng.common.util.SysConstant;
import com.huateng.data.vo.ResInfo;
import com.huateng.data.vo.params.*;
import com.huateng.web.service.MemberPointsRelateService;
import com.huateng.web.service.MemberPointsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Api(tags = "积分相关操作")
@RestController
public class MemberPointsController extends BaseController {

    @Resource
    private MemberPointsService memberPointsService;

    @Resource
    private MemberPointsRelateService memberPointsRelateService;

    @ApiOperation(value = "积分兑换", notes = "返回兑换结果对象")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "订单主键", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "custId", value = "会员号", required = true, dataType = "String"),
            @ApiImplicitParam(name = "date", value = "主机时间", required = true, dataType = "Date"),
            @ApiImplicitParam(name = "exchangeGoods", value = "商品信息", required = true, dataType = "List<ExchangeGoods>")
    })
    @PostMapping("/operate/exchange")
    public ResInfo exchange(String id, String custId, Date date, @RequestBody List<ExchangeGoods> exchangeGoods) throws Exception {
        ResInfo resInfo = new ResInfo();

        /**计算总积分**/
        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal goodsTotal;
        for (ExchangeGoods e : exchangeGoods) {
            //有折扣
            if (e.getDiscountType() != null && !"".equalsIgnoreCase(e.getDiscountType())) {
                totalPrice = totalPrice.add(e.getTotalPrice());
            } else {
                //无折扣
                goodsTotal = e.getUnitPrice().multiply(e.getNumber());
                if (goodsTotal.compareTo(e.getTotalPrice()) != 0) {
                    throw new BusinessException(getErrorInfo(SysConstant.E02000001)); //单价总价不一致
                }
                totalPrice = totalPrice.add(goodsTotal);
            }
        }

        if (totalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(getErrorInfo(SysConstant.E02000003));
        }

        /**
         * 开始兑换
         */
        ExchangeResult exchangeResult = this.memberPointsService.exchange(id, custId, date, exchangeGoods, totalPrice);

        resInfo.setData(JacksonUtil.toJson(exchangeResult));
        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }

    @ApiOperation(value = "积分计算", notes = "返回结果对象")
    @ApiImplicitParam(name = "computeBonus", value = "积分计算参数", required = true, dataType = "ComputeBonus")
    @PostMapping("/operate/computeBonus")
    public ResInfo computeBonus(@RequestBody ComputeBonus computeBonus)throws Exception{
        ResInfo resInfo = new ResInfo();


        List<BonusAccItf> result = this.memberPointsService.excuteComputeBonus(computeBonus);
        if(CollUtil.isNotEmpty(result)){
            resInfo.setData(JacksonUtil.toJson(result));
        }
        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }

    @ApiOperation(value = "积分入账", notes = "返回结果对象")
    @ApiImplicitParam(name = "bonusEnter", value = "入账参数", required = true, dataType = "BonusEnter")
    @PostMapping("/operate/bonusPlanEnter")
    public ResInfo bonusPlanEnter(@RequestBody BonusEnter bonusEnter) throws Exception{
        ResInfo resInfo = new ResInfo();

        AddPointsResult addPointsResult = this.memberPointsService.bonusPlanEnter(bonusEnter);
        CompletableFuture.runAsync(() -> {
            try {
                if(addPointsResult.getDelayMsg() != null){
                    this.memberPointsRelateService.sendWeChatMsg(addPointsResult.getDelayMsg(), SysConstant.SEND_WECHAT_MSG_ADJUST);
                }

                if(addPointsResult.getGenMsg() != null){
                    this.memberPointsRelateService.sendWeChatMsg(addPointsResult.getGenMsg(), SysConstant.SEND_WECHAT_MSG_PRODUCE);
                }
            }catch (Exception e){
                log.error("发送推送失败",e);
            }
        });

        resInfo.setData(JacksonUtil.toJson(addPointsResult));
        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }

    @ApiOperation(value = "积分冲正", notes = "返回结果对象")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "autoBonusReversal", value = "冲正参数", required = true, dataType = "AutoBonusReversal"),
    })
    @PostMapping("/operate/bonusReversal")
    public ResInfo bonusReversal(@RequestBody AutoBonusReversal autoBonusReversal) throws Exception{
        ResInfo resInfo = new ResInfo();

        if(SysConstant.REVERSE_TYPE_All.equals(autoBonusReversal.getReverseType())){
            this.memberPointsService.bonusReversalAll(autoBonusReversal);//整单冲正
        }else{
            this.memberPointsService.bonusReversalPart(autoBonusReversal);//部分冲正
        }

        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }

    @ApiOperation(value = "积分冲正(老交易)", notes = "返回结果对象")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "autoTblBonusReversal", value = "冲正参数", required = true, dataType = "AutoTblBonusReversal"),
    })
    @PostMapping("/operate/tblBonusReversal")
    public ResInfo tblBonusReversal(@RequestBody AutoTblBonusReversal autoTblBonusReversal) throws Exception{
        ResInfo resInfo = new ResInfo();

        if(SysConstant.REVERSE_TYPE_All.equals(autoTblBonusReversal.getReverseType())){
            this.memberPointsService.tblBonusReversalAll(autoTblBonusReversal);
        }else{
            this.memberPointsService.tblBonusReversalPart(autoTblBonusReversal);
        }


        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }


    @ApiOperation(value = "积分调整", notes = "返回结果对象")
    @ApiImplicitParam(name = "adjustOrder", value = "积分调整参数", required = true, dataType = "AdjustOrder")
    @PostMapping("/operate/adjust")
    public ResInfo adjust(@RequestBody AdjustOrder adjustOrder)throws Exception{
        ResInfo resInfo = new ResInfo();

        SendWeChat sendWeChat = this.memberPointsService.excuteAdjust(adjustOrder);
        CompletableFuture.runAsync(() -> {
            try {
                if (sendWeChat != null) {
                    this.memberPointsRelateService.sendWeChatMsg(sendWeChat, SysConstant.SEND_WECHAT_MSG_EXTERNAL_ADJUST);
                }
            } catch (Exception e) {
                log.error("积分变动推送异常");
            }
        });


        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }

}
