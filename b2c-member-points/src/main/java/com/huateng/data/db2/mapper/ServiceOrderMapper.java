package com.huateng.data.db2.mapper;


import com.huateng.data.model.db2.ServiceOrder;


public interface ServiceOrderMapper {

    int insert(ServiceOrder serviceOrder);

    int updateByPrimaryKeySelective(ServiceOrder serviceOrder);

}