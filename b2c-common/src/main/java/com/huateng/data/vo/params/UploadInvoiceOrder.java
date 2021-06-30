package com.huateng.data.vo.params;

import com.huateng.data.model.db2.ServiceOrderDetail;
import lombok.Data;

import java.util.List;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/5/11
 * Time: 13:53
 * Description:
 */
@Data
public class UploadInvoiceOrder {
    private String reqSerialNo;
    private List<ServiceOrderDetail> goods;
}
