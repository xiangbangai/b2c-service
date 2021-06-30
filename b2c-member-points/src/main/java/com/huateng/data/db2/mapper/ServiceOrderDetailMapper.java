package com.huateng.data.db2.mapper;


import com.huateng.data.model.db2.ServiceOrderDetail;
import org.apache.ibatis.annotations.Param;

public interface ServiceOrderDetailMapper {

    int insert(ServiceOrderDetail record);

    void updateGoods(@Param("id") String id, @Param("detail") ServiceOrderDetail serviceOrderDetail);

    int updateReturnableNumber(ServiceOrderDetail orderDetail);
}