package com.huateng.data.db2.mapper;


import com.huateng.data.model.db2.ServiceBonusDetail;

import java.util.List;

public interface ServiceBonusDetailMapper {

    int insert(ServiceBonusDetail record);

    int updateByPrimaryKeySelective(ServiceBonusDetail record);

    int updateReturnableNumber(ServiceBonusDetail bonusDetail);

    void updateReturnableNumberToZeroByOrderId(String orderId);
}