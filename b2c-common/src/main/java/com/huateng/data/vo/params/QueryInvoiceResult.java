package com.huateng.data.vo.params;

import lombok.Data;

import java.util.List;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/6/24
 * Time: 16:33
 * Description:
 */
@Data
public class QueryInvoiceResult {
    private int status;
    private List<InvoiceInfo> data;
}
