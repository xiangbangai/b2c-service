package com.huateng.data.db2.mapper;

import com.huateng.data.model.db2.TblTxnDetail;

public interface TblTxnDetailMapper {

    int insert(TblTxnDetail txnDetail);

    int updateReturnFlag(TblTxnDetail txnDetail);
}