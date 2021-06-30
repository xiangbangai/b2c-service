package com.huateng.data.db2.mapper;


import com.huateng.data.model.db2.ServiceCustLimit;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

public interface ServiceCustLimitMapper {

    int insert(ServiceCustLimit record);

    int updateByPrimaryKeySelective(ServiceCustLimit record);
}