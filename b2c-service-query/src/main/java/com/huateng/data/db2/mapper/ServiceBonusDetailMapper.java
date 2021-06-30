package com.huateng.data.db2.mapper;


import com.huateng.data.model.db2.ServiceBonusDetail;

import java.util.List;

public interface ServiceBonusDetailMapper {

    List<ServiceBonusDetail> queryBonusDetailByOrderId(String orderId);
}