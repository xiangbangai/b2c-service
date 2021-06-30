package com.huateng.data.db2.mapper;

import com.huateng.data.model.db2.TblOrderDetail;

import java.util.List;

public interface TblOrderDetailMapper {

    List<TblOrderDetail> getByOrderId(String orderId);
}