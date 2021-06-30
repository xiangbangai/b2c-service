package com.huateng.data.db2.mapper;

import com.huateng.data.model.db2.TblAcctInf;
import org.apache.ibatis.annotations.Param;

public interface TblAcctInfMapper {

    TblAcctInf queryTblAcctInfByCustId(@Param("custId")String custId, @Param("acctType")String acctType);
}
