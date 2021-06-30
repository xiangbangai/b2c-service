package com.huateng.data.db2.mapper;

import com.huateng.data.model.db2.TblAcctInfPart;
import com.huateng.data.model.db2.TblCustInf;
import com.huateng.data.model.db2.TblCustInfAndAcct;
import org.apache.ibatis.annotations.Param;

public interface TblCustInfMapper {

    TblCustInf getCustInfo(@Param("id") String id);

    TblCustInfAndAcct getInfoAndAcct(@Param("id") String id);

    TblCustInf queryInfoById(@Param("custId") String custId);
}