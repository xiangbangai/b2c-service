package com.huateng.service.remote;

import com.huateng.data.model.db3.Edclistno;
import com.huateng.data.model.db3.PayJ;
import com.huateng.data.model.db3.SaleJ;
import com.huateng.data.vo.ResInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/4/17
 * Time: 13:36
 * Description:
 */
@FeignClient(value = "b2c-service-query", contextId = "queryInvoiceRemote", path = "/query/invoice")
public interface QueryInvoiceRemote {

    /**
     * 查询EdcListNo总数
     * @param edclistno
     * @return
     */
    @PostMapping("/queryEdcListNoCount")
    ResInfo queryEdcListNoCount(Edclistno edclistno);


    /**
     * 查询PayJ总数
     * @param payJ
     * @return
     */
    @PostMapping("/queryPayJCount")
    ResInfo queryPayJCount(PayJ payJ);


    /**
     * 查询SaleJ总数
     * @param saleJ
     * @return
     */
    @PostMapping("/querySaleJCount")
    ResInfo querySaleJCount(SaleJ saleJ);
}
