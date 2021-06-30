package com.huateng.web.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.huateng.base.BaseService;
import com.huateng.common.util.JacksonUtil;
import com.huateng.data.model.db2.ServiceInvoiceTxn;
import com.huateng.data.model.db2.ServiceOrderDetail;
import com.huateng.data.model.db3.SaleJ;
import com.huateng.data.vo.ResInfo;
import com.huateng.data.vo.params.QueryInvoiceResult;
import com.huateng.data.vo.params.UploadInvoice;
import com.huateng.data.vo.params.UploadInvoiceOrder;
import com.huateng.service.remote.MemberPointsRelateRemote;
import com.huateng.service.remote.QueryInvoiceRemote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/4/1
 * Time: 16:49
 * Description:
 */
@Slf4j
@Service
public class InvoiceService extends BaseService {

    @Resource
    private MemberPointsRelateRemote memberPointsRelateRemote;
    @Resource
    private QueryInvoiceRemote queryInvoiceRemote;

    /**
     * 更新商品信息
     */
    public void updateGoods(UploadInvoiceOrder uploadInvoiceOrder, String channel) throws Exception {
        ResInfo resInfo = this.memberPointsRelateRemote.updateGoods(uploadInvoiceOrder, channel);
        getResJson(resInfo);
    }

    /**
     * 插发票库
     * @param uploadInvoice
     */
    public void uploadInvoice(UploadInvoice uploadInvoice) throws Exception {
        ResInfo resInfo = this.memberPointsRelateRemote.uploadInvoice(uploadInvoice);
        getResJson(resInfo);
    }

    /**
     * 保存开票流水
     * @param serviceInvoiceTxn
     * @throws Exception
     */
    public void saveInvoiceTxn(ServiceInvoiceTxn serviceInvoiceTxn) throws Exception {
        ResInfo resInfo = this.memberPointsRelateRemote.saveInvoiceTxn(serviceInvoiceTxn);
        getResJson(resInfo);
    }

    /**
     * 查询开票金额
     * @param saleJ
     */
    public QueryInvoiceResult queryInvoiceAmountOfMoney(SaleJ saleJ) throws Exception {
        ResInfo resInfo = this.queryInvoiceRemote.queryInvoiceAmountOfMoney(saleJ);
        return JacksonUtil.toObject(getResJson(resInfo),  new TypeReference<QueryInvoiceResult>(){});
    }

    /**
     * 新增商品信息
     * @param serviceOrderDetail
     * @param channel
     * @param reqSerialNo
     */
    public void insertGoods(ServiceOrderDetail serviceOrderDetail, String channel, String reqSerialNo) throws Exception {
        ResInfo resInfo = this.memberPointsRelateRemote.insertGoods(serviceOrderDetail, channel, reqSerialNo);
        getResJson(resInfo);
    }
}
