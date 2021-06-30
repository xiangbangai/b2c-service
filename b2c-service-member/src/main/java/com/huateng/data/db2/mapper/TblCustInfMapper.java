package com.huateng.data.db2.mapper;

import com.huateng.data.model.db2.TblCustInf;

public interface TblCustInfMapper {

    int updateByCustIdSelective(TblCustInf record);

    int insertSelective(TblCustInf record);
}