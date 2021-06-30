package com.huateng.service.remote;

import com.huateng.data.model.db3.SaleJ;
import com.huateng.data.vo.ResInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/6/24
 * Time: 15:25
 * Description: 查询开票信息
 */
@FeignClient(value = "b2c-service-query", contextId = "queryInvoiceRemote", path = "/query/invoice")
public interface QueryInvoiceRemote {

    @PostMapping("/queryInvoiceAmountOfMoney")
    ResInfo queryInvoiceAmountOfMoney(SaleJ saleJ);
}
