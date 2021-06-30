package com.huateng.data.vo.params;

import com.huateng.data.model.db2.ServiceOrderDetail;
import com.huateng.data.model.db3.PayJ;
import com.huateng.data.model.db3.SaleJ;
import lombok.Data;

import java.util.List;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/3/31
 * Time: 10:33
 * Description:
 */
@Data
public class UploadInvoice {
    private String acctId;
    private String edcTaxType;
    private Integer edcFlag;
    private String edcSeqNo;
    private Integer invoiceFlag; //是否开票 0-不开 1-开票
    private List<SaleJ> sale;
    private List<PayJ> pay;
    private UploadInvoiceOrder order;
}
