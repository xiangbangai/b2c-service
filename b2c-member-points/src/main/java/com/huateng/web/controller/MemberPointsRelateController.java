package com.huateng.web.controller;

import com.huateng.base.BaseController;
import com.huateng.common.util.JacksonUtil;
import com.huateng.common.util.SysConstant;
import com.huateng.data.model.db2.ServiceInvoiceTxn;
import com.huateng.data.model.db2.ServiceOrder;
import com.huateng.data.model.db2.ServiceOrderDetail;
import com.huateng.data.vo.ResInfo;
import com.huateng.data.vo.params.*;
import com.huateng.web.service.MemberPointsRelateService;
import com.huateng.web.service.ValidateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/4/5
 * Time: 17:07
 * Description:
 */
@Slf4j
@Api(tags = "积分关联操作")
@RestController
public class MemberPointsRelateController extends BaseController {

    @Resource
    private MemberPointsRelateService memberPointsRelateService;
    @Resource
    private ValidateService validateService;

    @ApiOperation(value = "保存订单", notes = "返回结果对象")
    @ApiImplicitParam(name = "serviceOrder", value = "订单对象", required = true, dataType = "ServiceOrder")
    @PostMapping("/relate/saveOrder")
    public ResInfo saveOrder(@RequestBody ServiceOrder serviceOrder) throws Exception{
        ResInfo resInfo = new ResInfo();

        this.validateService.validateForString(serviceOrder.getChannel(), SysConstant.E02000006); //channel不可为空
        this.validateService.validateForString(serviceOrder.getReqSerialNo(), SysConstant.E02000009); //reqSerialNo不可为空
        this.validateService.validateForObject(serviceOrder.getOrderType(), SysConstant.E02000014); //orderType不可为空

        ServiceOrder order = this.memberPointsRelateService.saveOrder(serviceOrder);

        resInfo.setData(JacksonUtil.toJson(order));
        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return  resInfo;
    }

    @ApiOperation(value = "上传发票流水", notes = "流水入发票库")
    @ApiImplicitParam(name = "uploadInvoice", value = "发票信息", required = true, dataType = "UploadInvoice")
    @PostMapping("/relate/upLoadInvoice")
    public ResInfo upLoadInvoice(@RequestBody UploadInvoice uploadInvoice) throws Exception {
        ResInfo resInfo = new ResInfo();

        this.memberPointsRelateService.uploadInvoice(uploadInvoice);

        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }

    @ApiOperation(value = "更新商品信息", notes = "更新商品收货信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goods", value = "商品信息", required = true, dataType = "ServiceOrderDetail"),
            @ApiImplicitParam(name = "serialNo", value = "流水", required = true, dataType = "String"),
            @ApiImplicitParam(name = "channel", value = "渠道", required = true, dataType = "String"),
    })
    @PostMapping("/relate/updateGoods")
    public ResInfo updateGoods(@RequestBody UploadInvoiceOrder uploadInvoiceOrder, @RequestParam String channel) throws Exception{
        ResInfo resInfo = new ResInfo();

        this.memberPointsRelateService.updateGoods(uploadInvoiceOrder, channel);

        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }

    @ApiOperation(value = "保存开票流水并开票", notes = "保存开票流水并开票")
    @ApiImplicitParam(name = "serviceInvoiceTxn", value = "开票信息", required = true, dataType = "ServiceInvoiceTxn")
    @PostMapping("/relate/saveInvoiceTxn")
    public ResInfo saveInvoiceTxn(@RequestBody ServiceInvoiceTxn serviceInvoiceTxn) throws Exception {
        ResInfo resInfo = new ResInfo();

        this.memberPointsRelateService.saveInvoiceTxn(serviceInvoiceTxn);

        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }


    @ApiOperation(value = "更新订单", notes = "更新订单")
    @ApiImplicitParam(name = "serviceOrder", value = "订单信息", required = true, dataType = "ServiceOrder")
    @PostMapping("/relate/updateServiceOrder")
    public ResInfo updateOrderFail(@RequestBody ServiceOrder serviceOrder) {
        ResInfo resInfo = new ResInfo();

        this.memberPointsRelateService.updateServiceOrder(serviceOrder);

        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }


    @ApiOperation(value = "推送微信消息", notes = "推送微信消息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sendWeChat", value = "微信推送参数", required = true, dataType = "SendWeChat"),
            @ApiImplicitParam(name = "type", value = "类型", required = true, dataType = "Integer")
    })
    @PostMapping("/relate/sendWeChatMsg")
    public void sendWeChatMsg(@RequestBody SendWeChat sendWeChat, Integer type) throws Exception {
        this.memberPointsRelateService.sendWeChatMsg(sendWeChat, type);
    }

    @ApiOperation(value = "新增商品信息", notes = "新增商品信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goods", value = "商品信息", required = true, dataType = "ServiceOrderDetail"),
            @ApiImplicitParam(name = "channel", value = "渠道", required = true, dataType = "String"),
            @ApiImplicitParam(name = "reqSerialNo", value = "请求流水", required = true, dataType = "String")
    })
    @PostMapping("/relate/insertGoods")
    public ResInfo insertGoods(@RequestBody ServiceOrderDetail serviceOrderDetail,@RequestParam String channel, @RequestParam String reqSerialNo) throws Exception{
        ResInfo resInfo = new ResInfo();

        this.memberPointsRelateService.insertGoods(serviceOrderDetail, channel, reqSerialNo);

        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }

}
