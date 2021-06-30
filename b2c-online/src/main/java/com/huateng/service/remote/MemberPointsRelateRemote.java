package com.huateng.service.remote;

import com.huateng.data.model.db2.ServiceInvoiceTxn;
import com.huateng.data.model.db2.ServiceOrder;
import com.huateng.data.model.db2.ServiceOrderDetail;
import com.huateng.data.vo.ResInfo;
import com.huateng.data.vo.params.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/4/5
 * Time: 17:23
 * Description:
 */
@FeignClient(value = "b2c-member-points", contextId = "memberPointsRelateRemote", path = "/memberPoints/relate")
public interface MemberPointsRelateRemote {

    /**
     * 保存订单
     * @param order
     * @return
     */
    @PostMapping("/saveOrder")
    ResInfo saveOrder(@RequestBody ServiceOrder order);

    /**
     * 上传流水
     * @param uploadInvoice
     * @return
     */
    @PostMapping("/upLoadInvoice")
    ResInfo uploadInvoice(@RequestBody UploadInvoice uploadInvoice);

    /**
     * 更新商品信息
     * @param uploadInvoiceOrder
     * @param channel
     * @return
     */
    @PostMapping("/updateGoods")
    ResInfo updateGoods(@RequestBody UploadInvoiceOrder uploadInvoiceOrder, @RequestParam String channel);

    /**
     * 保存开票流水
     * @param serviceInvoiceTxn
     * @return
     */
    @PostMapping("/saveInvoiceTxn")
    ResInfo saveInvoiceTxn(@RequestBody ServiceInvoiceTxn serviceInvoiceTxn);

    /**
     * 更新订单状态
     * @param serviceOrder
     * @return
     */
    @PostMapping("/updateServiceOrder")
    ResInfo updateServiceOrder(@RequestBody ServiceOrder serviceOrder);


    /**
     * 推送微信消息
     * @param sendWeChat
     * @param type
     */
    @PostMapping("/sendWeChatMsg")
    void sendWeChatMsg(@RequestBody SendWeChat sendWeChat, @RequestParam Integer type);

    /**
     * 新增商品信息
     * @param serviceOrderDetail
     * @param channel
     * @param reqSerialNo
     * @return
     */
    @PostMapping("/insertGoods")
    ResInfo insertGoods(@RequestBody ServiceOrderDetail serviceOrderDetail,@RequestParam String channel, @RequestParam String reqSerialNo);
}
