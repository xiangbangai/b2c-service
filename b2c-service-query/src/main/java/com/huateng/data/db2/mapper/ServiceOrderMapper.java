package com.huateng.data.db2.mapper;


import com.huateng.data.model.db2.ServiceOrder;
import com.huateng.data.model.db2.ServiceOrderTxnInfo;
import com.huateng.data.vo.params.MemberPointsTxnParams;
import com.huateng.data.vo.params.OrderInfo;

import java.util.List;

public interface ServiceOrderMapper {

    ServiceOrder getOrderInfo(OrderInfo orderInfo);

    ServiceOrder queryOrderById(String id);

    List<ServiceOrderTxnInfo> queryTxnInfo(MemberPointsTxnParams memberPointsTxnParams);
}