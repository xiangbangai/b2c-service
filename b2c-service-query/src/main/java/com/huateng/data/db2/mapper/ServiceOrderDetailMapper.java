package com.huateng.data.db2.mapper;


import com.huateng.data.model.db2.ServiceOrderDetail;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface ServiceOrderDetailMapper {

    List<ServiceOrderDetail> queryOrderDetailByOrderId(String orderId);

    List<ServiceOrderDetail> queryOrderDetailByOrderList(@Param("beginDate") Date beginDate, @Param("endDate") Date endDate,
                                                         @Param("custId") String custId, @Param("ids") Object[] id);
}