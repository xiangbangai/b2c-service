package com.huateng.data.db2.mapper;


import com.huateng.data.model.db2.TblOrder;
import org.apache.ibatis.annotations.Param;

public interface TblOrderMapper {
    TblOrder queryTblOrderInfo(@Param("acqSsn") String acqSsn, @Param("acctId") String acctId);

    TblOrder queryTblOrderByOrderId(@Param("orderId")String orderId);
}