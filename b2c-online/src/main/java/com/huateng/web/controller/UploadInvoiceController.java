package com.huateng.web.controller;

import com.huateng.base.BaseController;
import com.huateng.common.util.JacksonUtil;
import com.huateng.common.util.SysConstant;
import com.huateng.data.model.db2.ServiceInvoiceTxn;
import com.huateng.data.model.db2.ServiceOrderDetail;
import com.huateng.data.model.db2.TblCustInf;
import com.huateng.data.model.db3.PayJ;
import com.huateng.data.model.db3.SaleJ;
import com.huateng.data.vo.json.Request;
import com.huateng.data.vo.json.Response;
import com.huateng.data.vo.params.InvoiceInfo;
import com.huateng.data.vo.params.QueryInvoiceResult;
import com.huateng.data.vo.params.SendInvoice;
import com.huateng.data.vo.params.UploadInvoice;
import com.huateng.web.service.CustService;
import com.huateng.web.service.InvoiceService;
import com.huateng.web.service.ValidateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/3/31
 * Time: 10:27
 * Description: 上传发票信息
 */
@Slf4j
@RestController
public class UploadInvoiceController extends BaseController {

    @Resource
    private CustService custService;
    @Resource
    private InvoiceService invoiceService;
    @Resource
    private ValidateService validateService;


    /**
     * 上传发票信息
     * @return
     */
    @PostMapping("/invoice/upload")
    public Response uploadInvoice() throws Exception {
        Response response = new Response();
        response.setResCode(SysConstant.SYS_SUCCESS);
        Request request;
        UploadInvoice uploadInvoice;
        request = getObject(UploadInvoice.class);
        uploadInvoice = (UploadInvoice) request.getData();
        //更新收货信息
        if (uploadInvoice.getOrder() != null) {
            this.validateService.validateForString(uploadInvoice.getOrder().getReqSerialNo(), SysConstant.E01000012); //reqSerialNo不可为空
            this.validateService.validateForArrayList(uploadInvoice.getOrder().getGoods(), SysConstant.E01000035); //goods不可为空
            for (ServiceOrderDetail s : uploadInvoice.getOrder().getGoods()) {
                this.validateService.validateForString(s.getId(), SysConstant.E01000024); //id不可为空
                this.validateService.validateForObject(s.getDeliveryDate(), SysConstant.E01000037); //deliveryDate格式错误
            }
            this.invoiceService.updateGoods(uploadInvoice.getOrder(), request.getChannel());
        }

        this.validateService.validateForArrayList(uploadInvoice.getSale(), SysConstant.E01000056); //sale不可为空
        this.validateService.validateForArrayList(uploadInvoice.getPay(), SysConstant.E01000057); //pay不可为空

        for (SaleJ saleJ : uploadInvoice.getSale()) {
            saleJ.setWorkdate(saleJ.getWorkdate() + SysConstant.WORK_DATE_TIME);
            saleJ.setDt(saleJ.getDt() + SysConstant.WORK_DATE_TIME);
            saleJ.setTime(SysConstant.WORK_DATE + saleJ.getTime() );
        }
        for (PayJ payJ : uploadInvoice.getPay()) {
            payJ.setWorkdate(payJ.getWorkdate() + SysConstant.WORK_DATE_TIME);
            payJ.setDt(payJ.getDt() + SysConstant.WORK_DATE_TIME);
            payJ.setTime(SysConstant.WORK_DATE + payJ.getTime() );
        }


        /**上传流水**/
        this.invoiceService.uploadInvoice(uploadInvoice);

        /**需要开票**/
        if (uploadInvoice.getInvoiceFlag() != null && uploadInvoice.getInvoiceFlag() == 1 && uploadInvoice.getAcctId() != null) {
            /**校验手机参数**/
            validateChannelForMobile(uploadInvoice.getAcctId(), request.getChannel());

            TblCustInf tblCustInf = this.custService.queryCustInfo(uploadInvoice.getAcctId());

            if (tblCustInf != null) {
                if (tblCustInf.getIsAcceptEinvoice() != 1) {
                    log.info("客户[{}]未开通自动开票，不能自动开具电子发票", tblCustInf.getCustId());
                    return response;
                }
                if (tblCustInf.getOpenId() == null || "".equalsIgnoreCase(tblCustInf.getOpenId())) {
                    log.info("openid为空，客户[{}]不能自动开具电子发票", tblCustInf.getCustId());
                    return response;
                }

                /**
                 * 返回status状态是200，data有记录，amount大于0，并且categoryDesc是空的
                 * 满足条件可开票，否则不开票
                 */
                QueryInvoiceResult queryInvoiceResult = null;
                try {
                    queryInvoiceResult = this.invoiceService.queryInvoiceAmountOfMoney(uploadInvoice.getSale().get(0));
                } catch (Exception e) {
                    log.error("查询开票金额异常...",e);
                    return response;
                }

                log.info("客户[{}]开票查询结果:{}", tblCustInf.getCustId(), queryInvoiceResult.toString());
                //检查开票
                if (queryInvoiceResult != null && queryInvoiceResult.getStatus() == 200) {
                    boolean amount = false;
                    boolean categoryDesc = false;
                    if (!queryInvoiceResult.getData().isEmpty()) {
                        for (InvoiceInfo i : queryInvoiceResult.getData()) {
                            if (i.getAmount().compareTo(BigDecimal.ZERO) == 1) {
                                amount = true;
                            }
                            if (i.getCategoryDesc() != null && !"".equals(i.getCategoryDesc())) {
                                categoryDesc = true;
                            }
                        }
                        if (amount && !categoryDesc) {
                            SendInvoice sendInvoice = new SendInvoice();
                            BeanUtils.copyProperties(uploadInvoice.getSale().get(0), sendInvoice);
                            sendInvoice.setOpenId(tblCustInf.getOpenId());
                            sendInvoice.setWorkdate(sendInvoice.getWorkdate().substring(0,10));
                            String params = JacksonUtil.toJson(sendInvoice);
                            log.info("客户[{}]开票：{}", tblCustInf.getCustId(), params);

                            uploadInvoice = null;

                            //保存流水
                            ServiceInvoiceTxn serviceInvoiceTxn = new ServiceInvoiceTxn();
                            serviceInvoiceTxn.setId(getSnowId());
                            serviceInvoiceTxn.setChannel(request.getChannel());
                            serviceInvoiceTxn.setReqSerialNo(request.getReqSerialNo());
                            serviceInvoiceTxn.setSendParams(params);
                            serviceInvoiceTxn.setSendInvoice(sendInvoice);
                            this.invoiceService.saveInvoiceTxn(serviceInvoiceTxn);
                        }
                    }
                }
            }
        }

        return response;
    }
}
